import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import bind from 'bind-decorator';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Button, Header, Icon, Label, Segment, Table} from 'semantic-ui-react';

const style = require('./PersonalPanel.less');

@observer
export class PersonalPanel extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  @bind
  private onLogoutClick() {
    cls.profile.logout();
  }

  @bind
  private onBuyChipsClick() {
    cls.profile.buyChips();
  }

  render() {
    const username = rootStore.username;
    const balance = rootStore.game.balance;

    return (
      <Segment textAlign='center' className={style.main}>
        <Label attached='top' color='green'>User</Label>
        <Segment className={style.userContainer}>
          <Icon circular size="huge" name='user circle'/>
          <br/>
          <Header>{username}</Header>
        </Segment>
        <Segment className={style.balanceContainer} textAlign='left'>
          <Label attached='top' color='blue' style={{textAlign: 'center'}}>Balance</Label>
          <div style={{display: 'inline-block', fontSize: 20}}>
            <Icon name='dollar sign'/>
            {balance}
          </div>
        </Segment>

        <div className={style.buttonContainer} style={{flex: 0}}>
          <Button content='Buy Chips'
                  icon='payment' labelPosition='right' color='blue'
                  style={{margin: 0}}
                  onClick={this.onBuyChipsClick}
          />
        </div>
        <div className={style.buttonContainer}>
          <Button content='Create game'
                  primary
                  icon='add' labelPosition='right'
                  onClick={cls.ui.showCreateDlg}
          />
          <Button content='Join game'
                  primary
                  icon='linkify' labelPosition='right'
                  onClick={cls.ui.showJoinDlg}
          />

          <Button content='Log out'
                  icon='log out' labelPosition='right'
                  onClick={this.onLogoutClick}
          />
        </div>
      </Segment>
    );
  }

  // TODO: remove this
  private renderCurrentGameInfo() {
    const {playerId, currentGame, thisPlayer} = rootStore.game;
    if (!currentGame) {
      return (
        <div/>
      );
    }

    return (
      <div className={style.info}>
        <Table definition>
          <Table.Body>
            <Table.Row>
              <Table.Cell width={5}>Small Blind</Table.Cell>
              <Table.Cell width={3}>{currentGame.smallBlind}</Table.Cell>
            </Table.Row>
            <Table.Row>
              <Table.Cell>Big Blind</Table.Cell>
              <Table.Cell>{currentGame.smallBlind * 2}</Table.Cell>
            </Table.Row>
            <Table.Row>
              <Table.Cell>Round bet</Table.Cell>
              <Table.Cell>{currentGame.roundBet}</Table.Cell>
            </Table.Row>
          </Table.Body>
        </Table>
      </div>
    );
  }
}
