import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';

export class UiController {

  @action.bound
  public showCreateDlg() {
    rootStore.ui.createDlg.opened = true;
    rootStore.ui.createDlg.name = '';
  }

  @action.bound
  public hideCreateDlg() {
    rootStore.ui.createDlg.opened = false;
  }

  @action.bound
  public createGame() {
    const {canCreate, name, smallBlind, buyIn} = rootStore.ui.createDlg;
    if (canCreate) {
      cls.games.createGame(name.trim(), smallBlind, buyIn);
    }
    this.hideCreateDlg();
  }

  @action.bound
  public updateCreateDlg(key: string, value: any) {
    const {balance} = rootStore.game;
    const {createDlg} = rootStore.ui;
    createDlg[key] = value;

    const {name: nameEditValue, smallBlindEditValue, buyInEditValue} = createDlg;

    const name = nameEditValue.trim();
    const smallBlind = parseInt(smallBlindEditValue);
    const buyIn = parseInt(buyInEditValue);
    if (name.length == 0 || isNaN(smallBlind) || isNaN(smallBlind) || buyIn > balance || buyIn < smallBlind * 5) {
      createDlg.canCreate = false;
    } else {
      createDlg.canCreate = true;
      createDlg.smallBlind = smallBlind;
      createDlg.buyIn = buyIn;
    }
  }

  @action.bound
  public showJoinDlg() {
    rootStore.ui.joinDlg.opened = true;
  }

  @action.bound
  public hideJoinDlg() {
    rootStore.ui.joinDlg.opened = false;
  }

  @action.bound
  public joinGame(gameId: string) {
    cls.games.join(gameId);
    this.hideJoinDlg();
  }
}
