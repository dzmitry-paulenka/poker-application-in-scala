import {cls} from 'app/controller/Controllers';
import {PersonalPanel} from 'app/view/components/PersonalPanel';
import {StatusPanel} from 'app/view/components/StatusPanel';
import {TableView} from 'app/view/components/TableView';
import {action} from 'mobx';
import {observer} from 'mobx-react';
import * as React from 'react';

import {Grid} from 'semantic-ui-react'

@observer
export class LobbyPage extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);

    this.state = {
      username: '',
      password: ''
    }
  }

  @action.bound
  private onInputChanged(e, {name, value}) {
    this.setState({[name]: value})
  }

  @action.bound
  private onLoginKeyPress(e) {
    if (e.charCode === 32 || e.charCode === 13) {
      // Prevent the default action to stop scrolling when space is pressed
      e.preventDefault();
      this.onLoginClick();
    }
  };

  @action.bound
  private onLoginClick() {
    const {username, password} = this.state;
    cls.profile.login(username, password)
  }

  render() {
    const {username, password} = this.state;

    return (
      <Grid.Column stretched style={{width: 1200, height: 650}}>
        {/*<Grid stretched style={{border: '1px solid blue'}}>*/}
        <Grid stretched>
          <Grid.Row stretched>
            <Grid.Column width={3}>
              <PersonalPanel/>
            </Grid.Column>
            <Grid.Column width={10}>
              <TableView/>
            </Grid.Column>
            <Grid.Column width={3}>
              <StatusPanel/>
            </Grid.Column>
          </Grid.Row>
        </Grid>
      </Grid.Column>
    );
  }
}
