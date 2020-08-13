import {loadStripe, Stripe} from '@stripe/stripe-js'

window.onload = () => {
    const key = new URLSearchParams(document.location.search).get("key")!!
    const sessionId = new URLSearchParams(document.location.search).get("sessionId")!!
    loadStripe(key).then(function (stripe: Stripe | null){
        stripe?.redirectToCheckout({
            // Make the id field from the Checkout Session creation API response
            // available to this file, so you can provide it as argument here
            // instead of the {{CHECKOUT_SESSION_ID}} placeholder.
            sessionId: sessionId
          }).then(function (result: any) {
            console.log("error");
            // If `redirectToCheckout` fails due to a browser or network
            // error, display the localized error message to your customer
            // using `result.error.message`.
          })
          .catch(function (err: any) {
            console.log(err);
          });
    })
}