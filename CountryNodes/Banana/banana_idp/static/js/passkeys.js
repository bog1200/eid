async function registerPasswordless() {
    const p = new Passwordless.Client({
        apiKey: 'romailapp:public:bb5bfe42b65e4ed38729038b16b95ead'
      });
      
      // Fetch the returned registration token from the backend.
      const pc = await fetch('/passkeys/create').then((r) => r.json());
      const registerToken = pc.token;
      
      // Register the token with the end-user's device.
      const { token, error } = await p.register(registerToken);
      if (token) {
        // Successfully registered!
        // await fetch('/passkeys/save?token=' + token);
      } else {
        console.error(error);
      }
    
}

async function showPasskeys() {
  console.log('showPasskeys');
  fetch('/passkeys/list').then((r) => r.json()).then((r) => {
    console.log(r);
    console.log(r.passkeys.values[0]);
    for (let i = 0; i < r.passkeys.values.length; i++) {
      console.log(r.passkeys.values[i]);
      const name = r.passkeys.values[i].nickname;
      const id = r.passkeys.values[i].descriptor.id;
      const created = r.passkeys.values[i].createdAt;
      const lastUsed = r.passkeys.values[i].lastUsedAt;
      const discoverable = r.passkeys.values[i].isDiscoverable;

      document.getElementById('passkeys').innerHTML += 'Name: ' + name + '<br>';
      document.getElementById('passkeys').innerHTML += 'ID: ' + id + '<br>';
      document.getElementById('passkeys').innerHTML += 'Created: ' + created + '<br>';
      document.getElementById('passkeys').innerHTML += 'Last Used: ' + lastUsed + '<br>';
      document.getElementById('passkeys').innerHTML += 'Discoverable: ' + discoverable + '<br>';
      document.getElementById('passkeys').innerHTML += '<br>';
    }
  }
  );
}

// Instantiate a passwordless client using your API public key.
