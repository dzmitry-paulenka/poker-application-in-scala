import {Config} from 'app/config/Config';
import {BuyChipsCommand} from 'app/controller/ConnectionController';
import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';

export class ProfileController {

  @action.bound
  public async init(): Promise<void> {
    const authString = localStorage.getItem('auth');
    if (authString == null) {
      // no saved auth info
      return;
    }

    const auth = JSON.parse(authString);
    await this.httpPost('check-token', auth)
      .then(({valid}) => {
        if (valid) {
          rootStore.isLoggedIn = true;
          rootStore.username = auth.username;
          rootStore.authToken = auth.authToken;
          cls.connection.connect();
        }
      });
  }

  @action.bound
  public async loginOrSignup(username: string, password: string, isLogin: boolean): Promise<void> {
    const auth = await this.httpPost(isLogin ? 'login' : 'signup', {username, password});

    rootStore.isLoggedIn = true;
    rootStore.username = auth.username;
    rootStore.authToken = auth.authToken;
    localStorage.setItem('auth', JSON.stringify(auth));
    cls.connection.connect();
  }

  @action.bound
  public logout(): void {
    rootStore.isLoggedIn = false;
    rootStore.username = null;
    rootStore.authToken = null;
    localStorage.removeItem('auth');
    cls.connection.disconnect();
  }

  @action.bound
  public buyChips(): void {
    cls.connection.send(
      new BuyChipsCommand(1000)
    );
  }

  public async httpPost(url: String, data: any): Promise<any> {
    const requestUrl = `${Config.usersApiUrl()}/${url}`;
    const response = await fetch(requestUrl, {
      redirect: 'manual',
      method: 'POST',
      headers: new Headers({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }),
      body: JSON.stringify(data)
    });

    switch (response.status) {
      case 401:
        return Promise.reject('Unauthorized');
      case 409:
        return Promise.reject('User already exists');
      case 404:
        return Promise.reject('User not found');
    }

    return await response.json() as any;
  }
}
