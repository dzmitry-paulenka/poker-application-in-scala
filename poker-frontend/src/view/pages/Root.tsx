import {rootStore} from 'app/store/RootStore';
import {LobbyPage} from 'app/view/pages/LobbyPage';
import {LoginPage} from 'app/view/pages/LoginPage';
import {observer} from 'mobx-react';
import * as React from 'react';

@observer
export class Root extends React.Component<any, any> {
  render() {
    const isLoggedIn = rootStore.isLoggedIn;

    return (
      <div style={{display: 'flex', width: '100vw', height: '100vh', overflow: 'auto'}}>
        {isLoggedIn && <LobbyPage/>}
        {!isLoggedIn && <LoginPage/>}
      </div>
    )
  }
}
