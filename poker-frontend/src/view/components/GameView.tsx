import {cls} from 'app/controller/Controllers';
import {PLAYER_COUNT_LIMIT} from 'app/store/GameStore';
import {rootStore} from 'app/store/RootStore';
import {TableView} from 'app/view/components/TableView';
import bind from 'bind-decorator';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Button, Form, Header, Icon, Menu, Modal, Segment, Table} from 'semantic-ui-react';

const style = require('./GameView.less');

@observer
export class GameView extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  @bind
  private onGameSelected(gameId: string) {
    cls.games.selectGame(gameId);
  }

  render() {
    const currentGame = rootStore.game.currentGame;
    const playerGames = rootStore.game.playerGames;

    return (
      <div className={style.main}>
        <Menu pointing inverted color='green'>
          {playerGames.map(g => (
            <Menu.Item
              key={g.id}
              content={`${g.name}`}
              active={currentGame && currentGame.id == g.id}
              onClick={() => this.onGameSelected(g.id)}/>
          ))}
        </Menu>

        {this.renderCreateGameModal()}
        {this.renderJoinGameModal()}

        {currentGame && <TableView style={{flex: 1}}/>}
        {!currentGame && this.renderPlaceholder()}
      </div>
    );
  }

  private renderPlaceholder() {
    const loading = rootStore.game.currentGame;

    return (
      <Segment placeholder style={{flex: 1}}>
        <Header icon>
          <Icon name='lightbulb outline'/>
          You're not playing any games at the moment.
        </Header>
        <Segment.Inline>
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
        </Segment.Inline>
      </Segment>
    );
  }

  renderCreateGameModal() {
    const {canCreate, opened, name, smallBlind, smallBlindEditValue, buyIn, buyInEditValue} = rootStore.ui.createDlg;

    return (
      <Modal size='tiny' open={opened} onClose={cls.ui.hideCreateDlg}>
        <Modal.Header>Create New Game</Modal.Header>
        <Modal.Content>
          <Form>
            <Form.Input fluid label='Name' placeholder='Name'
                        value={name}
                        onChange={e => cls.ui.updateCreateDlg('name', e.target.value)}
            />
            <Form.Input fluid label='Small blind' placeholder='Small blind'
                        value={smallBlindEditValue}
                        onChange={e => cls.ui.updateCreateDlg('smallBlindEditValue', e.target.value)}
            />
            <Form.Input fluid label='Buy in' placeholder='Buy in'
                        value={buyInEditValue}
                        onChange={e => cls.ui.updateCreateDlg('buyInEditValue', e.target.value)}
            />
          </Form>
        </Modal.Content>
        <Modal.Actions>
          <Button
            content='Cancel'
            onClick={cls.ui.hideCreateDlg}
          />
          <Button
            positive
            content='Create'
            disabled={!canCreate}
            onClick={cls.ui.createGame}
          />
        </Modal.Actions>
      </Modal>
    );
  }

  renderJoinGameModal() {
    const {balance, activeGames} = rootStore.game;
    const {opened} = rootStore.ui.joinDlg;

    return (
      <Modal open={opened} onClose={cls.ui.hideJoinDlg}>
        <Modal.Header>Join Active Game</Modal.Header>
        <Modal.Content>
          <Table celled>
            <Table.Header>
              <Table.Row>
                <Table.HeaderCell width={5}>Game name</Table.HeaderCell>
                <Table.HeaderCell width={3}>Small Blind</Table.HeaderCell>
                <Table.HeaderCell width={3}>Buy In</Table.HeaderCell>
                <Table.HeaderCell width={3}>Players Count</Table.HeaderCell>
                <Table.HeaderCell width={3}>Join</Table.HeaderCell>
              </Table.Row>
            </Table.Header>

            <Table.Body>
              {activeGames.map(ag => {
                  const canJoin = ag.buyIn <= balance &&
                    !rootStore.game.thisPlayerIsInGame(ag.id) &&
                    ag.playerCount < PLAYER_COUNT_LIMIT;

                  return <Table.Row key={ag.id}>
                    <Table.Cell>{ag.name}</Table.Cell>
                    <Table.Cell>{ag.smallBlind}</Table.Cell>
                    <Table.Cell>{ag.buyIn}</Table.Cell>
                    <Table.Cell>{ag.playerCount}</Table.Cell>
                    <Table.Cell>
                      <Button
                        style={{width: '100%'}}
                        primary
                        content='Join'
                        disabled={!canJoin}
                        onClick={() => cls.ui.joinGame(ag.id)}
                      />
                    </Table.Cell>
                  </Table.Row>;
                }
              )}
            </Table.Body>
          </Table>

        </Modal.Content>
        <Modal.Actions>
          <Button
            content='Cancel'
            onClick={cls.ui.hideJoinDlg}
          />
        </Modal.Actions>
      </Modal>
    );
  }
}
