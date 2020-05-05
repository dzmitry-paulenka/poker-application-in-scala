import {Config} from 'app/config/Config';
import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';
import * as Noty from 'noty';
import ReconnectingWebSocket from 'reconnecting-websocket';

export type EventListener<T> = (event: T) => void;

export interface ClientEvent {
  eventType: string;
}

export class Pong implements ClientEvent {
  public readonly eventType: string = 'pong';
}

export class BuyChipsCommand implements ClientEvent {
  public readonly eventType: string = 'buy-chips-command';

  constructor(public readonly amount: number) {
  }
}

export class AddBotCommand implements ClientEvent {
  public readonly eventType: string = 'add-bot-command';

  constructor(public readonly gameId: string,
              public readonly botType: string) {
  }
}

export class RemoveBotCommand implements ClientEvent {
  public readonly eventType: string = 'remove-bot-command';

  constructor(public readonly gameId: string,
              public readonly botId: string) {
  }
}

export class CreateGameCommand implements ClientEvent {
  public readonly eventType: string = 'create-game-command';

  constructor(public readonly name: string,
              public readonly smallBlind: number,
              public readonly buyIn: number) {
  }
}

export type GameTransition = {
  transition: string,
  playerId?: string,
  amount?: number
}

export class TransitionCommand implements ClientEvent {
  public readonly eventType: string = 'transition-command';

  constructor(public readonly gameId: string,
              public readonly transition: GameTransition) {
  }
}

export class ConnectionController {
  private socket: ReconnectingWebSocket = null;
  private listenerMap: Map<string, Array<EventListener<any>>> = new Map();

  @action.bound
  public connect() {
    this.disconnect();

    const username = rootStore.username;
    const authToken = rootStore.authToken;
    const wsUrl = `${Config.websocketBaseUrl()}?authToken=${encodeURIComponent(authToken)}`;

    const socket = new ReconnectingWebSocket(wsUrl);
    this.socket = socket;

    socket.addEventListener('open', () => {
      // no-op
    });

    socket.addEventListener('close', () => {
      // no-op
    });

    socket.addEventListener('error', e => {
      console.log('Websocket error: ', e)
    });

    socket.addEventListener('message', msg => {
      const event = JSON.parse(msg.data);
      const eventType = event.eventType;
      if (eventType == 'ping') {
        this.send(new Pong());
        return;
      }

      if (eventType == 'error-message') {
        new Noty({
          type: 'error',
          text: event.error,
          theme: 'semanticui',
          layout: 'topLeft',
          timeout: 5000,
          progressBar: true
        }).show();

        return;
      }

      const listeners = this.listenerMap.get(eventType) || [];
      for (let listener of listeners) {
        listener(event);
      }
    });
  }

  public disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }

  public listenEvents<T>(eventType: string, listener: EventListener<T>) {
    const listeners = this.listenerMap.get(eventType);
    if (!listeners)
      this.listenerMap.set(eventType, [listener]);
    else
      listeners.push(listener);
  }

  public send(event: ClientEvent) {
    if (this.socket) {
      this.socket.send(JSON.stringify({
        ...event
      }));
    }
  }
}
