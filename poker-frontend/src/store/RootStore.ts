import BoxModel from 'app/store/BoxModel';
import {observable} from 'mobx';

export class RootStore {
  @observable
  public isLoggedIn: boolean = false;

  @observable
  public username: string = null;

  @observable
  public boxes: Array<BoxModel> = [
    new BoxModel(10, 10),
    new BoxModel(200, 200),
    new BoxModel(700, 400, 200)
  ];
}

export const rootStore = new RootStore();
