import {cls} from 'app/controller/Controllers';
import {Card} from 'app/store/GameStore';
import {rootStore} from 'app/store/RootStore';
import {Assert} from 'app/util/Assert';
import bind from 'bind-decorator';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Button, Image, Input, Popup, Segment, Table} from 'semantic-ui-react';

const style = require('./TableView.less');

export const CardView: React.FC<any> = (props) => {
  const card: Card = props.card;
  const width: number = props.width;
  const height: number = width * 3 / 2;
  const isFront = !card;
  if (!card) {
    return (
      <div className={`${style.card} ${style.back}`} style={{width: width, height: height}}/>
    )
  }

  const rank = card.displayRank;
  const suit = card.suit;
  const color = card.cssColor;

  return (
    <div className={style.card} style={{width: width, height: height, color: color}}>
      <div className={style.rank}
           style={{fontSize: height / 2, lineHeight: `${height / 2}px`}}
      >
        {rank}
      </div>
      <Image className={style.suit} src={`assets/card-suit-${suit}.png`}/>
    </div>
  );
};

@observer
export class TableView extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  @bind
  private editRaiseAmount(editValue: string) {
    cls.games.changeRaiseAmount(editValue);
  }

  @bind
  private doFold() {
    cls.games.doFold();
  }

  @bind
  private doCheck() {
    cls.games.doCheck();
  }

  @bind
  private doCall() {
    cls.games.doCall();
  }

  @bind
  private doRaise() {
    cls.games.doRaise();
  }

  render() {
    const {playerId, currentGame, thisPlayer, balance} = rootStore.game;
    const {canFold, canCheck, canCall} = rootStore.game;

    Assert.yes(currentGame, 'currentGame should be defined at this point');
    Assert.yes(thisPlayer, 'thisPlayer should be defined at this point');

    const card0 = thisPlayer.hand[0];
    // const card1 = thisPlayer.hand[1];
    const card1 = null;

    const isCall = true;
    const callDisabled = true;
    const foldDisabled = true;
    const checkCallText = isCall ? 'Call' : 'Check';

    return (
      <Segment className={style.main}>
        <div className={style.top}>
          <Image src='assets/table-base.svg'/>
        </div>

        <div className={style.bottom}>
          <div className={style.cards}>
            <CardView card={card0} width={100}/>
            <CardView card={card1} width={100}/>
          </div>

          <div className={style.buttons}>
            <Button primary content='Fold' disabled={!canFold} onClick={this.doFold}/>
            {canCheck && <Button primary content='Check' disabled={!canCheck} onClick={this.doCheck}/>}
            {!canCheck && <Button primary content='Call' disabled={!canCall} onClick={this.doCall}/>}
            {this.renderRaiseButton()}
          </div>
        </div>
      </Segment>
    );
  }

  private renderRaiseButton() {
    const {playerId, currentGame, thisPlayer, currentBalance} = rootStore.game;
    const {raiseAmount, raiseAmountNum, raiseAmountValid, canRaise} = rootStore.game;

    const okDisabled = !raiseAmountValid || raiseAmountNum > currentBalance;
    const allInDisabled = !raiseAmountValid;

    return <Popup
      trigger={
        <Button primary disabled={!canRaise}>Raise</Button>
      }
      on='click'
      position='right center'
    >
      <Input
        icon='dollar sign'
        iconPosition='left'
        placeholder='Raise amount'
        value={raiseAmount}
        error={!raiseAmountValid}
        onChange={e => this.editRaiseAmount(e.target.value)}
      />

      <div style={{marginTop: 10}}>
        <Button color='green' content='Ok' disabled={okDisabled}
                onClick={this.doRaise}
        />
        <Button color='green' content='All In' disabled={allInDisabled}
                onClick={() => this.editRaiseAmount(`${currentBalance}`)}
        />
      </div>
    </Popup>;
  }
}
