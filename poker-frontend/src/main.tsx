import {cls} from 'app/controller/Controllers';
import {Root} from 'app/view/pages/Root';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import {hot} from 'react-hot-loader/root';

const bootstrap = async () => {
  await cls.initialize();

  const rootElement = document.getElementById('root');
  const App = hot(({}) => (<Root/>));
  ReactDOM.render(<App/>, rootElement);
};

bootstrap();
