import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';

export class ConnectionController {

  @action.bound
  public init() {
    const username = rootStore.username;
  }
}
