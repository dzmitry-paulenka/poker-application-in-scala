import {cls} from 'app/controller/Controllers';
import {rootStore} from 'app/store/RootStore';
import bind from 'bind-decorator';
import {observer} from 'mobx-react';
import * as React from 'react';
import * as style from './PersonalPanel.css';
import {Button, Grid, Header, Icon, Label, Segment} from 'semantic-ui-react';

@observer
export class PersonalPanel extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  @bind
  private onLogoutClick() {
    cls.profile.logout();
  }

  render() {
    const username = rootStore.username;

    return (
      <Grid stretched>
        <Grid.Row stretched>
          <Grid.Column stretched>
            <Segment raised textAlign={'center'}
                     style={{display: 'flex', flexDirection: 'column', justifyContent: 'space-between'}}>
              <Segment>
                <Icon circular size="huge" name='user circle'/>
                <br/>
                <Header>{username}</Header>
              </Segment>

              <Segment basic>
                <Grid stretched>
                  <Button
                    content='logout' icon='log out' labelPosition='right'
                    style={{width: '100vh', margin: 0}}
                    onClick={this.onLogoutClick}
                  />
                </Grid>
              </Segment>
            </Segment>
          </Grid.Column>
        </Grid.Row>
      </Grid>
    );
  }
}
