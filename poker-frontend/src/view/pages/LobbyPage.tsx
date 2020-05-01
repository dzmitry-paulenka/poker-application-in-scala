import {rootStore} from 'app/store/RootStore';
import {GameView} from 'app/view/components/GameView';
import {PersonalPanel} from 'app/view/components/PersonalPanel';
import {observer} from 'mobx-react';
import * as React from 'react';

import {Dimmer, Grid, Loader, Segment} from 'semantic-ui-react'

@observer
export class LobbyPage extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  render() {
    const {playerId} = rootStore.game;
    if (!playerId) {
      return this.renderLoader();
    }

    return (
      <Grid.Column stretched style={{minWidth: 980, maxWidth: 980, height: 650}}>
        <Grid stretched>
          <Grid.Row stretched>
            <Grid.Column width={4}>
              <PersonalPanel/>
            </Grid.Column>
            <Grid.Column width={12}>
              <GameView/>
            </Grid.Column>
          </Grid.Row>
        </Grid>
      </Grid.Column>
    );
  }

  private renderLoader() {
    return (
      <Grid.Column stretched style={{width: 1200, height: 650}}>
        <Segment>
          <Dimmer active inverted>
            <Loader/>
          </Dimmer>
        </Segment>
      </Grid.Column>
    )
  }
}
