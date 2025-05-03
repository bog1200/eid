// Instantiate a passwordless client using your API public key.
async function loginPasswordless() {
    const p = new Passwordless.Client({
        apiKey: "romailapp:public:bb5bfe42b65e4ed38729038b16b95ead"
    });

    const { token, error } = await p.signinWithDiscoverable();
    if (token) {
        console.log(token);
        console.log(window.location.search+"&token="+token);
        if (window.location.search == "") {
            window.location.href = "/passkeys/verify?token="+token;
        }
        else {
            window.location.href = "/passkeys/verify"+window.location.search+"&token="+token;
        }
    }
               
        // // submit form with token
        // const form = document.createElement("form");
        // form.method = "POST";
        // form.action = "/sso/login";
        // const hiddenField = document.createElement("input");
        // hiddenField.type = "hidden";
        // hiddenField.name = "passkey";
        // hiddenField.value = token;
        // form.appendChild(hiddenField);
        // document.body.appendChild(form);
        // form.submit();
   
}
//loginPasswordless();