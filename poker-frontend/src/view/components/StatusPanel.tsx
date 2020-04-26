import {observer} from 'mobx-react';
import * as React from 'react';
import * as style from './StatusPanel.css';
import {Segment} from 'semantic-ui-react';

@observer
export class StatusPanel extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  render() {


    return (
      <Segment raised   >
        StatusPanel
      </Segment>
    );
  }
}
