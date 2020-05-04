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
      <div style={{display: 'flex', maxWidth: 940, minWidth: 940, height: 622, margin: 'auto'}}>
        <PersonalPanel/>
        <GameView/>
      </div>
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
