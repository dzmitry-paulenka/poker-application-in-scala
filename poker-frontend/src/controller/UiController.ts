import {CreateGameCommand, GameTransition, TransitionCommand} from 'app/controller/ConnectionController';
import {cls} from 'app/controller/Controllers';
import {ActiveGame, Game, Player} from 'app/store/GameStore';
import {rootStore} from 'app/store/RootStore';
import {Assert} from 'app/util/Assert';
import bind from 'bind-decorator';
import {action, observable} from 'mobx';

import {deserialize, serializable, list, object} from 'serializr'

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
    const {createDlg} = rootStore.ui;
    createDlg[key] = value;

    const {name: nameEditValue, smallBlindEditValue, buyInEditValue} = createDlg;

    const name = nameEditValue.trim();
    const smallBlind = parseInt(smallBlindEditValue);
    const buyIn = parseInt(buyInEditValue);
    if (name.length == 0 || isNaN(smallBlind) || isNaN(smallBlind) || buyIn < smallBlind * 5) {
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
    console.log("Joining the game: ", gameId);
    cls.games.join(gameId);
  }
}
