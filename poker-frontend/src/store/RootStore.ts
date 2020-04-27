import {GameStore} from 'app/store/GameStore';
import {UiStore} from 'app/store/UiStore';
import {observable} from 'mobx';

export class RootStore {
  @observable
  public isLoggedIn: boolean = false;

  @observable
  public username: string = null;

  @observable
  public game: GameStore = new GameStore();

  @observable
  public ui: UiStore = new UiStore();
}

export const rootStore = new RootStore();
