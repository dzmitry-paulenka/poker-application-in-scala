import {rootStore} from 'app/store/RootStore';
import {LobbyPage} from 'app/view/pages/LobbyPage';
import {LoginPage} from 'app/view/pages/LoginPage';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Container, Grid} from 'semantic-ui-react';

@observer
export class Root extends React.Component<any, any> {
  render() {
    const isLoggedIn = rootStore.isLoggedIn;

    return (
      <Grid.Row stretched>
        <Grid centered style={{height: '100vh'}} verticalAlign='middle'>
          {isLoggedIn && <LobbyPage/>}
          {!isLoggedIn && <LoginPage/>}
        </Grid>
      </Grid.Row>
    )
  }
}
