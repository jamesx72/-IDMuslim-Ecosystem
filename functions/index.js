const functions = require("firebase-functions");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();

// Optional Stripe config (read from functions config or env)
const stripeSecretKey = process.env.STRIPE_SECRET_KEY || (functions.config().stripe ? functions.config().stripe.secret_key : "sk_test_placeholder");
const stripeWebhookSecret = process.env.STRIPE_WEBHOOK_SECRET || (functions.config().stripe ? functions.config().stripe.webhook_secret : "");
const stripe = require("stripe")(stripeSecretKey);

/**
 * Helper: Find user document reference by Firebase UID, email or customer ID
 */
async function resolveUserDoc(userId, email, customerId) {
  if (userId) {
    const directRef = db.collection("users").document(userId);
    const snap = await directRef.get();
    if (snap.exists) return { ref: directRef, uid: userId, data: snap.data() };
  }

  if (email) {
    const qSnap = await db.collection("users").where("email", "==", email.toLowerCase().trim()).limit(1).get();
    if (!qSnap.empty) {
      const doc = qSnap.docs[0];
      return { ref: doc.ref, uid: doc.id, data: doc.data() };
    }
  }

  if (customerId) {
    const qSnap = await db.collection("users").where("stripeCustomerId", "==", customerId).limit(1).get();
    if (!qSnap.empty) {
      const doc = qSnap.docs[0];
      return { ref: doc.ref, uid: doc.id, data: doc.data() };
    }
  }

  return null;
}

/**
 * 1. HTTP Cloud Function: Stripe Webhook Listener
 * Endpoint: https://<region>-<project-id>.cloudfunctions.net/stripeWebhook
 */
exports.stripeWebhook = functions.https.onRequest(async (req, res) => {
  const sig = req.headers["stripe-signature"];
  let event;

  try {
    if (stripeWebhookSecret && sig) {
      event = stripe.webhooks.constructEvent(req.rawBody, sig, stripeWebhookSecret);
    } else {
      // Fallback for direct payload verification in sandbox/dev
      event = req.body;
    }
  } catch (err) {
    console.error("⚠️ Stripe Webhook signature verification failed:", err.message);
    return res.status(400).send(`Webhook Error: ${err.message}`);
  }

  const eventType = event.type || event.event_type;
  console.log(`🔔 Received Stripe Webhook Event: ${eventType}`);

  try {
    switch (eventType) {
      case "payment_intent.succeeded": {
        const paymentIntent = event.data.object;
        const userId = paymentIntent.metadata?.userId || paymentIntent.metadata?.uid;
        const customerEmail = paymentIntent.receipt_email || paymentIntent.metadata?.email;
        const customerId = paymentIntent.customer;
        const tier = paymentIntent.metadata?.tier || (paymentIntent.amount > 5000 ? "WAQF_PATRON" : "EMERALD");
        const billingCycle = paymentIntent.metadata?.billingCycle || "ANNUAL";
        const isRenewal = paymentIntent.metadata?.isRenewal === "true" || paymentIntent.metadata?.action === "RENEWAL";

        const userRecord = await resolveUserDoc(userId, customerEmail, customerId);
        if (userRecord) {
          const durationDays = billingCycle === "ANNUAL" ? 365 : 30;
          const currentExpiry = userRecord.data?.membershipExpiry || Date.now();
          const baseTime = (isRenewal && currentExpiry > Date.now()) ? currentExpiry : Date.now();
          const expiryTimestamp = baseTime + durationDays * 24 * 60 * 60 * 1000;

          await userRecord.ref.set({
            membershipTier: tier,
            membershipStatus: "ACTIVE",
            membershipCycle: billingCycle,
            membershipExpiry: expiryTimestamp,
            membershipAutoRenew: true,
            hasPaidForPdf: true,
            stripeCustomerId: customerId || userRecord.data?.stripeCustomerId || null,
            lastPaymentMethod: "STRIPE_CARD",
            lastPaymentStatus: "SUCCEEDED",
            lastPaymentAmount: (paymentIntent.amount || 0) / 100,
            lastPaymentCurrency: paymentIntent.currency || "eur",
            lastTransactionRef: paymentIntent.id,
            updatedAt: Date.now(),
          }, { merge: true });

          // Record payment receipt in subcollection
          await userRecord.ref.collection("payments").doc(paymentIntent.id).set({
            paymentId: paymentIntent.id,
            amount: (paymentIntent.amount || 0) / 100,
            currency: (paymentIntent.currency || "eur").toUpperCase(),
            provider: "STRIPE",
            status: "SUCCEEDED",
            tier: tier,
            billingCycle: billingCycle,
            isRenewal: isRenewal,
            description: `Renouvellement Formule ${tier} (${billingCycle}) via Stripe SDK`,
            timestamp: Date.now(),
          }, { merge: true });

          console.log(`✅ User ${userRecord.uid} subscription renewed/activated to ${tier} via Stripe PaymentIntent ${paymentIntent.id}`);
        }
        break;
      }

      case "checkout.session.completed": {
        const session = event.data.object;
        const userId = session.client_reference_id || session.metadata?.userId;
        const customerEmail = session.customer_details?.email || session.customer_email;
        const customerId = session.customer;
        const tier = session.metadata?.tier || (session.amount_total > 5000 ? "WAQF_PATRON" : "EMERALD");
        const billingCycle = session.metadata?.billingCycle || (session.mode === "subscription" ? "ANNUAL" : "MONTHLY");

        const userRecord = await resolveUserDoc(userId, customerEmail, customerId);
        if (userRecord) {
          const durationDays = billingCycle === "ANNUAL" ? 365 : 30;
          const expiryTimestamp = Date.now() + durationDays * 24 * 60 * 60 * 1000;

          await userRecord.ref.set({
            membershipTier: tier,
            membershipStatus: "ACTIVE",
            membershipCycle: billingCycle,
            membershipExpiry: expiryTimestamp,
            membershipAutoRenew: session.mode === "subscription",
            hasPaidForPdf: true,
            stripeCustomerId: customerId || null,
            stripeSubscriptionId: session.subscription || null,
            lastPaymentMethod: "CARD",
            lastPaymentStatus: "SUCCEEDED",
            lastPaymentAmount: (session.amount_total || 0) / 100,
            lastPaymentCurrency: session.currency || "eur",
            updatedAt: Date.now(),
          }, { merge: true });

          console.log(`✅ User ${userRecord.uid} subscription upgraded to ${tier} via Stripe Checkout`);
        }
        break;
      }

      case "customer.subscription.updated":
      case "customer.subscription.created": {
        const subscription = event.data.object;
        const customerId = subscription.customer;
        const status = subscription.status; // active, past_due, canceled, incomplete
        const expiryTimestamp = subscription.current_period_end * 1000;
        const cancelAtPeriodEnd = subscription.cancel_at_period_end;
        const tier = subscription.metadata?.tier || "EMERALD";

        const userRecord = await resolveUserDoc(subscription.metadata?.userId, null, customerId);
        if (userRecord) {
          const isActive = status === "active" || status === "trialing";
          const membershipStatus = isActive ? "ACTIVE" : (status === "past_due" ? "PAST_DUE" : "EXPIRED");

          await userRecord.ref.set({
            membershipTier: isActive ? tier : "STANDARD",
            membershipStatus: membershipStatus,
            membershipExpiry: expiryTimestamp,
            membershipAutoRenew: !cancelAtPeriodEnd,
            stripeSubscriptionId: subscription.id,
            lastPaymentStatus: status.toUpperCase(),
            updatedAt: Date.now(),
          }, { merge: true });

          console.log(`✅ Subscription ${subscription.id} for user ${userRecord.uid} updated to status ${membershipStatus}`);
        }
        break;
      }

      case "customer.subscription.deleted": {
        const subscription = event.data.object;
        const customerId = subscription.customer;
        const userRecord = await resolveUserDoc(subscription.metadata?.userId, null, customerId);
        if (userRecord) {
          await userRecord.ref.set({
            membershipTier: "STANDARD",
            membershipStatus: "EXPIRED",
            membershipAutoRenew: false,
            updatedAt: Date.now(),
          }, { merge: true });

          console.log(`⚠️ User ${userRecord.uid} subscription expired/deleted`);
        }
        break;
      }

      case "invoice.payment_succeeded": {
        const invoice = event.data.object;
        const customerId = invoice.customer;
        const userRecord = await resolveUserDoc(invoice.metadata?.userId, invoice.customer_email, customerId);
        if (userRecord) {
          const periodEnd = invoice.lines?.data?.[0]?.period?.end;
          const expiryTimestamp = periodEnd ? periodEnd * 1000 : (Date.now() + 30 * 24 * 60 * 60 * 1000);

          await userRecord.ref.set({
            membershipStatus: "ACTIVE",
            membershipExpiry: expiryTimestamp,
            lastPaymentStatus: "SUCCEEDED",
            hasPaidForPdf: true,
            updatedAt: Date.now(),
          }, { merge: true });
        }
        break;
      }

      case "invoice.payment_failed": {
        const invoice = event.data.object;
        const userRecord = await resolveUserDoc(invoice.metadata?.userId, invoice.customer_email, invoice.customer);
        if (userRecord) {
          await userRecord.ref.set({
            membershipStatus: "PAST_DUE",
            lastPaymentStatus: "FAILED",
            updatedAt: Date.now(),
          }, { merge: true });
        }
        break;
      }

      default:
        console.log(`ℹ️ Unhandled Stripe event: ${eventType}`);
    }

    return res.status(200).json({ received: true });
  } catch (error) {
    console.error("❌ Error handling Stripe webhook:", error);
    return res.status(500).json({ error: error.message });
  }
});

/**
 * 2. HTTP Cloud Function: PayPal Webhook Listener
 * Endpoint: https://<region>-<project-id>.cloudfunctions.net/paypalWebhook
 */
exports.paypalWebhook = functions.https.onRequest(async (req, res) => {
  const event = req.body;
  const eventType = event.event_type;

  console.log(`🔔 Received PayPal Webhook Event: ${eventType}`);

  try {
    const resource = event.resource || {};
    const customId = resource.custom_id || resource.subscriber?.custom_id || resource.metadata?.userId;
    const subscriberEmail = resource.subscriber?.email_address || resource.payer?.email_address;
    const subscriptionId = resource.id || resource.billing_agreement_id;

    const userRecord = await resolveUserDoc(customId, subscriberEmail, null);

    switch (eventType) {
      case "BILLING.SUBSCRIPTION.ACTIVATED":
      case "PAYMENT.SALE.COMPLETED": {
        if (userRecord) {
          const durationDays = (resource.billing_info?.cycle_executions?.[0]?.tenure_type === "ANNUAL") ? 365 : 30;
          const expiryTimestamp = Date.now() + durationDays * 24 * 60 * 60 * 1000;
          const tier = resource.plan_id?.includes("WAQF") ? "WAQF_PATRON" : "EMERALD";

          await userRecord.ref.set({
            membershipTier: tier,
            membershipStatus: "ACTIVE",
            membershipExpiry: expiryTimestamp,
            membershipCycle: durationDays === 365 ? "ANNUAL" : "MONTHLY",
            membershipAutoRenew: true,
            hasPaidForPdf: true,
            paypalSubscriptionId: subscriptionId || null,
            lastPaymentMethod: "PAYPAL",
            lastPaymentStatus: "COMPLETED",
            updatedAt: Date.now(),
          }, { merge: true });

          console.log(`✅ User ${userRecord.uid} PayPal subscription activated`);
        }
        break;
      }

      case "BILLING.SUBSCRIPTION.CANCELLED":
      case "BILLING.SUBSCRIPTION.EXPIRED":
      case "BILLING.SUBSCRIPTION.SUSPENDED": {
        if (userRecord) {
          await userRecord.ref.set({
            membershipTier: "STANDARD",
            membershipStatus: "EXPIRED",
            membershipAutoRenew: false,
            updatedAt: Date.now(),
          }, { merge: true });

          console.log(`⚠️ User ${userRecord.uid} PayPal subscription canceled/expired`);
        }
        break;
      }

      case "PAYMENT.SALE.DENIED": {
        if (userRecord) {
          await userRecord.ref.set({
            membershipStatus: "PAYMENT_FAILED",
            lastPaymentStatus: "DENIED",
            updatedAt: Date.now(),
          }, { merge: true });
        }
        break;
      }

      default:
        console.log(`ℹ️ Unhandled PayPal event: ${eventType}`);
    }

    return res.status(200).json({ received: true });
  } catch (error) {
    console.error("❌ Error handling PayPal webhook:", error);
    return res.status(500).json({ error: error.message });
  }
});

/**
 * 3. Firestore Trigger: Real-time Member Document Monitor
 * Listens on users/{userId} changes to log audit history & send real-time alerts
 */
exports.onMembershipDocumentUpdated = functions.firestore
  .document("users/{userId}")
  .onUpdate(async (change, context) => {
    const beforeData = change.before.data() || {};
    const afterData = change.after.data() || {};
    const userId = context.params.userId;

    const statusChanged = beforeData.membershipStatus !== afterData.membershipStatus;
    const tierChanged = beforeData.membershipTier !== afterData.membershipTier;
    const expiryChanged = beforeData.membershipExpiry !== afterData.membershipExpiry;

    if (statusChanged || tierChanged || expiryChanged) {
      console.log(`🔄 Real-time subscription change detected for user ${userId}:`, {
        fromTier: beforeData.membershipTier,
        toTier: afterData.membershipTier,
        fromStatus: beforeData.membershipStatus,
        toStatus: afterData.membershipStatus,
      });

      // Write security activity log for the user
      const isUpgrade = afterData.membershipStatus === "ACTIVE" && afterData.membershipTier !== "STANDARD";
      const logAction = isUpgrade ? "MEMBERSHIP_ACTIVATED_CLOUD" : (afterData.membershipStatus === "EXPIRED" ? "MEMBERSHIP_EXPIRED_CLOUD" : "MEMBERSHIP_UPDATED_CLOUD");
      const logDetail = `Statut d'adhésion: ${afterData.membershipTier || "STANDARD"} (${afterData.membershipStatus || "INACTIF"}) synchronisé en temps réel.`;

      await change.after.ref.collection("activity_logs").add({
        action: logAction,
        details: logDetail,
        timestamp: Date.now(),
        source: "CLOUD_FUNCTION_WEBHOOK_SYNC",
      });

      // Send FCM notification if device token is stored
      if (afterData.fcmToken) {
        const messageTitle = isUpgrade ? "✨ Adhésion Numérique Confirmée" : "ℹ️ Mise à jour de votre Adhésion";
        const messageBody = isUpgrade
          ? `Votre formule ${afterData.membershipTier} est active. Votre Digital ID a été actualisé en temps réel.`
          : `Votre statut d'adhésion est désormais: ${afterData.membershipStatus}.`;

        try {
          await admin.messaging().send({
            token: afterData.fcmToken,
            notification: {
              title: messageTitle,
              body: messageBody,
            },
            data: {
              type: "MEMBERSHIP_UPDATE",
              membershipTier: afterData.membershipTier || "STANDARD",
              membershipStatus: afterData.membershipStatus || "STANDARD",
              membershipExpiry: String(afterData.membershipExpiry || 0),
            },
          });
        } catch (fcmErr) {
          console.warn("FCM push notification skipped/failed:", fcmErr.message);
        }
      }
    }
  });

/**
 * 4. Scheduled Function: Nightly Membership Expiration Checker
 * Runs every 24 hours to automatically mark passed subscriptions as EXPIRED
 */
exports.checkExpiredMembershipsCron = functions.pubsub
  .schedule("every 24 hours")
  .onRun(async (context) => {
    const now = Date.now();
    console.log(`⏰ Running scheduled membership expiration check at ${new Date(now).toISOString()}`);

    const expiredSnap = await db
      .collection("users")
      .where("membershipStatus", "==", "ACTIVE")
      .where("membershipExpiry", "<", now)
      .get();

    if (expiredSnap.empty) {
      console.log("✅ No expired active memberships found.");
      return null;
    }

    const batch = db.batch();
    expiredSnap.forEach((doc) => {
      batch.update(doc.ref, {
        membershipStatus: "EXPIRED",
        membershipTier: "STANDARD",
        updatedAt: now,
      });
    });

    await batch.commit();
    console.log(`✅ Successfully transitioned ${expiredSnap.size} expired memberships to EXPIRED state.`);
    return null;
  });
