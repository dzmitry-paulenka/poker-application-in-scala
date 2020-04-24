import {GameStore} from 'app/store/GameStore';
import {observable} from 'mobx';

export class RootStore {
  @observable
  public isLoggedIn: boolean = false;

  @observable
  public username: string = null;

  @observable
  public game: GameStore = new GameStore();
}

export const rootStore = new RootStore();
