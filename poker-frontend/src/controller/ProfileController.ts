import {BuyChipsCommand} from 'app/controller/ConnectionController';
import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';

export class ProfileController {

  @action.bound
  public init() {
    const username = localStorage.getItem('username');
    if (username) {
      rootStore.isLoggedIn = true;
      rootStore.username = username;
      cls.connection.connect();
    }
  }

  @action.bound
  public login(username: string, password: string): void {
    rootStore.isLoggedIn = true;
    rootStore.username = username.trim();
    cls.connection.connect();
    localStorage.setItem('username', username);
  }

  @action.bound
  public logout(): void {
    rootStore.isLoggedIn = false;
    rootStore.username = null;
    localStorage.removeItem('username');
  }

  @action.bound
  public buyChips(): void {
    cls.connection.send(
      new BuyChipsCommand(1000)
    );
  }
}
