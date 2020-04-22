import {cls} from 'app/controller/Controllers';
import {Root} from 'app/view/pages/Root';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {hot} from 'react-hot-loader/root';


cls.initialize();


// render react DOM
const App = hot(({}) => (
  <Root/>
));

ReactDOM.render(
  <App/>,
  document.getElementById('root')
);
