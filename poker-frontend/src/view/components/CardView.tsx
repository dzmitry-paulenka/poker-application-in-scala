import BoxModel from 'app/store/BoxModel';
import {observer} from 'mobx-react';
import * as React from 'react';
import {Segment} from 'semantic-ui-react';
import * as style from './CardView.css';

@observer
export class CardView extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  render() {
    return (
      <Segment style={{border: '1px solid blue'}}>
        CardView
      </Segment>
    );
  }
}
