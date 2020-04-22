import {cls} from 'app/controller/Controllers';
import {action} from 'mobx';
import {observer} from 'mobx-react';
import * as React from 'react';

import {Button, Form, Grid, Header, Message, Segment} from 'semantic-ui-react'

@observer
export class LoginPage extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);

    this.state = {
      username: '',
      password: '',
      hasErrors: false
    }
  }

  @action.bound
  private onInputChanged(e, {name, value}) {
    this.setState({[name]: value})
  }

  @action.bound
  private onLoginKeyPress(e) {
    this.setState({hasErrors: false});
    if (e.charCode === 32 || e.charCode === 13) {
      // Prevent the default action to stop scrolling when space is pressed
      e.preventDefault();
      this.onLoginClick();
    }
  };

  @action.bound
  private onLoginClick() {
    const {username, password} = this.state;
    if (!username || username.trim().length === 0) {
      this.setState({hasErrors: true});
      return;
    }

    cls.profile.login(username, password)
  }

  render() {
    const {username, password, hasErrors} = this.state;

    return (
      <Grid.Column style={{maxWidth: 450}}>
        <Header as='h2' color='teal' textAlign='center'>
          Log-in to your account
        </Header>
        <Form size='large' error={hasErrors}>
          <Segment>
            <Form.Input
              fluid icon='user' iconPosition='left' placeholder='Login'
              name="username" value={username}
              onChange={this.onInputChanged}
            />
            <Form.Input
              fluid icon='lock' iconPosition='left' placeholder='Password' type='password'
              name="password" value={password} onChange={this.onInputChanged}
            />

            <Message
              error
              header='Error'
              content='The username/password are invalid.'
            />
            <Button
              color='teal' size='large' fluid
              onClick={this.onLoginClick}
              onKeyPress={this.onLoginKeyPress}
            >
              Login
            </Button>
          </Segment>
        </Form>
      </Grid.Column>
    );
  }
}
