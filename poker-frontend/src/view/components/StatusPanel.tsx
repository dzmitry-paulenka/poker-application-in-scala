import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Button, Label, Segment, Table} from 'semantic-ui-react';

const style = require('./StatusPanel.less');

@observer
export class StatusPanel extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  render() {
    return (
      <Segment textAlign='center' className={style.main}>
        <Label attached='top' color='green'>Game</Label>
        {this.renderCurrentGameInfo()}

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
        </div>
      </Segment>
    );
  }

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
