import BoxModel from 'app/store/BoxModel';
import {observer} from 'mobx-react';
import * as React from 'react';
import * as style from './GameView.css';
import {Segment} from 'semantic-ui-react';

@observer
export class GameView extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  render() {
    return (
      <Segment style={{border: '1px solid blue'}}>
        GameView
      </Segment>
    );
  }
}
