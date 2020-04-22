import BoxModel from 'app/store/BoxModel';
import {observer} from 'mobx-react';
import * as React from 'react';
import * as style from './TableView.css';
import {Segment} from 'semantic-ui-react';

@observer
export class TableView extends React.Component<any, any> {
  constructor(props: any, context: any) {
    super(props, context);
  }

  render() {
    return (
      <Segment piled>
        TableView
      </Segment>
    );
  }
}
