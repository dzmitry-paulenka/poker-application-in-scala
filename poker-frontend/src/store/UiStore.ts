import {observable} from 'mobx';

export class CreateDlg {
  @observable
  public opened: boolean = false;

  @observable
  public canCreate: boolean = false;

  @observable
  public name: string = '';

  @observable
  public smallBlindEditValue: string = '10';

  @observable
  public smallBlind: number = 10;

  @observable
  public buyInEditValue: string = '200';

  @observable
  public buyIn: number = 200;
}

export class JoinDlg {
  @observable
  public opened: boolean = false;
}

export class UiStore {
  @observable
  public createDlg: CreateDlg = new CreateDlg();

  @observable
  public joinDlg: JoinDlg = new JoinDlg();
}
