import {cls} from 'app/controller/Controllers';
import {action} from 'mobx';
import {observer} from 'mobx-react';
import * as React from 'react';

import {Button, Divider, Form, Grid, Header, Message, Segment} from 'semantic-ui-react'

@observer
export class LoginPage extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);

    this.state = {
      username: '',
      password: '',
      isLogin: true,
      error: '',
      hasErrors: false
    }
  }

  @action.bound
  private toggleLoginSignup() {
    this.setState({isLogin: !this.state.isLogin})
  }

  @action.bound
  private onInputChanged(e, {name, value}) {
    this.setState({
      [name]: value,
      hasErrors: false,
      error: false
    })
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
    const {username, password, isLogin} = this.state;
    if (!username || username.trim().length === 0) {
      this.setState({hasErrors: true});
      return;
    }

    cls.profile.loginOrSignup(username, password, isLogin).catch(error => {
      this.setState({
        hasErrors: true,
        error: error
      })
    })
  }

  render() {
    const {username, password, isLogin, hasErrors, error} = this.state;

    return (
      <Grid.Column style={{maxWidth: 450}}>
        <Header as='h2' color='teal' textAlign='center'>
          {isLogin ? 'Log-in to your account' : 'Sign up'}
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
              content={`Error: ${error}.`}
            />
            <Button
              color='teal' size='large' fluid
              onClick={this.onLoginClick}
              onKeyPress={this.onLoginKeyPress}
            >
              {isLogin ? 'Login' : 'Sign Up'}
            </Button>
          </Segment>
        </Form>
        <Divider horizontal>Or</Divider>
        <div style={{textAlign: 'center'}}>
          <a href="#" onClick={this.toggleLoginSignup}>{!isLogin ? 'Login' : 'Sign Up'}</a>
        </div>
      </Grid.Column>
    );
  }
}
