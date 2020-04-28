import {cls} from 'app/controller/Controllers';
import {Card, Player} from 'app/store/GameStore';
import {rootStore} from 'app/store/RootStore';
import {Assert} from 'app/util/Assert';
import bind from 'bind-decorator';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Button, Icon, Image, Input, Popup, Segment} from 'semantic-ui-react';

const style = require('./TableView.less');
const cx = require('classnames/bind').bind(style);

@observer
export class TableView extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);

    this.state = {
      raisePopupOpened: false
    };
  }

  @bind
  private openRaisePopup() {
    this.setState({raisePopupOpened: true});
  }

  @bind
  private closeRaisePopup() {
    this.setState({raisePopupOpened: false});
  }

  @bind
  private onRaiseOkClicked() {
    cls.games.doRaise();
    this.closeRaisePopup();
  }

  render() {
    const {currentGame, thisPlayer, cardsDealt, isShowdown} = rootStore.game;
    const {canFold, canCheck, canCall} = rootStore.game;

    Assert.yes(currentGame, 'currentGame should be defined at this point');
    Assert.yes(thisPlayer, 'thisPlayer should be defined at this point');

    const card0 = cardsDealt && thisPlayer.hand[0];
    const card1 = cardsDealt && thisPlayer.hand[1];
    const players = currentGame.players;

    return (
      <Segment className={style.main}>
        <div className={style.top}>
          <Image className={style.table} src='assets/table-base.svg'/>
          {players.map((player, index) =>
            this.renderPlayer(player, index)
          )}
          <div className={style.board}>
            {currentGame.board.map((card, index) =>
              this.renderCard(card, 'front', 30, index)
            )}
          </div>
          <div className={style.pot}>
            <Image src={`assets/chip.png`}/>$&nbsp;{currentGame.pot}
          </div>
        </div>

        <div className={style.bottom}>
          <div className={style.cards}>
            {this.renderCard(card0, cardsDealt ? 'front' : 'missing', 100)}
            {this.renderCard(card1, cardsDealt ? 'front' : 'missing', 100)}
          </div>

          <div className={style.buttons}>
            <Button primary content='Fold' disabled={!canFold} onClick={cls.games.doFold}/>
            {canCheck && <Button primary content='Check' disabled={!canCheck} onClick={cls.games.doCheck}/>}
            {!canCheck && <Button primary content='Call' disabled={!canCall} onClick={cls.games.doCall}/>}
            {this.renderRaiseButton()}
          </div>

          {isShowdown && <Button className={style.nextRound}
                                 content='Next round'
                                 icon='arrow right' labelPosition='right'
                                 onClick={() => cls.games.nextRound(currentGame.id)}
          />}

          <Button className={style.leave}
                  content='Leave game'
                  icon='log out' labelPosition='right'
                  onClick={() => cls.games.leave(currentGame.id)}
          />

        </div>
      </Segment>
    );
  }

  private renderCard(card: Card, state: string = 'front', width: number, key: number = undefined) {
    const height: number = width * 3 / 2;

    const rank = card && card.displayRank;
    const suit = card && card.suit;
    const color = card && card.cssColor;

    const classes = cx({
      card: true,
      missing: state == 'missing',
      back: state == 'back',
      front: state == 'front'
    });

    if (state != 'front') {
      return (
        <div key={key} className={classes} style={{width: width, height: height, color: color}}/>
      );
    }

    return (
      <div key={key} className={classes} style={{width: width, height: height, color: color}}>
        <div className={style.rank} style={{fontSize: height / 2, lineHeight: `${height / 2}px`}}>
          {rank}
        </div>
        <Image
          className={style.suit}
          style={{height: '45%'}}
          src={`assets/card-suit-${suit}.png`}/>
      </div>
    );
  }

  private renderPlayer(player: Player, index: number) {
    const {thisPlayer, currentPlayer, dealerPlayer, currentGame, cardsDealt, isShowdown} = rootStore.game;

    const cardsVisible = player.hand.length > 0;
    const card0 = cardsVisible && player.hand[0];
    const card1 = cardsVisible && player.hand[1];
    const cardStatus = cardsVisible ? 'front' : 'back';

    const isDealer = player == dealerPlayer;
    const isCurrent = player == currentPlayer;
    const isSelf = player == thisPlayer;
    const showHand = cardsDealt && !isSelf && !player.sittingOut;
    const showCombo = !!player.resultComboName;

    const classNames = cx('player', `player${index}`, {'current': isCurrent}, {'self': isSelf});
    const classes = cx(
      'player',
      `player${index}`,
      {
        self: isSelf,
        current: isCurrent,
        sitting: player.sittingOut
      });

    return (
      <div key={player.id} className={classes}>
        {showHand && <div className={style.hand}>
          {this.renderCard(card0, cardStatus, 30)}
          {this.renderCard(card1, cardStatus, 30)}
        </div>}
        <div className={style.badge}>
          <div className={style.name}>{player.id}</div>
          <div className={style.balance}>$&nbsp;{player.balance}</div>
        </div>
        <div className={style.money}>
          {player.resultMoneyWon > 0 && <Icon name='winner'/>}
          <Image src={`assets/chip.png`}/>$&nbsp;{isShowdown ? player.resultMoneyWon : player.gameBet}
        </div>
        {showCombo && player.resultComboName && <div className={style.combo}>
          {player.resultComboName}
        </div>}
        {isDealer && <Image className={style.dealer} src={`assets/chip-dealer.png`}/>}
      </div>
    )
  }

  private renderRaiseButton() {
    const {thisPlayer, currentGame, canRaise, raiseAmount, raiseAmountNum, raiseAmountValid} = rootStore.game;

    const okDisabled = !raiseAmountValid ||
      raiseAmountNum == 0 ||
      thisPlayer.balance < currentGame.roundBet + raiseAmountNum - thisPlayer.roundBet;

    const allInAmount = thisPlayer.balance + thisPlayer.roundBet - currentGame.roundBet;
    const allInDisabled = !raiseAmountValid || allInAmount < 0;
    const raiseText = currentGame.roundBet == 0 ? 'Bet' : 'Raise';

    return <Popup
      trigger={
        <Button primary disabled={!canRaise}>{raiseText}</Button>
      }
      open={this.state.raisePopupOpened}
      onOpen={this.openRaisePopup}
      onClose={this.closeRaisePopup}
      on='click'
      position='right center'
    >
      <Input
        icon='dollar sign'
        iconPosition='left'
        placeholder='Raise amount'
        value={raiseAmount}
        error={!raiseAmountValid}
        onChange={e => cls.games.changeRaiseAmount(e.target.value)}
      />

      <div style={{marginTop: 10}}>
        <Button color='green' content='Ok' disabled={okDisabled}
                onClick={this.onRaiseOkClicked}
        />
        <Button color='green' content='All In' disabled={allInDisabled}
                onClick={() => cls.games.changeRaiseAmount(`${allInAmount}`)}
        />
      </div>
    </Popup>;
  }
}
