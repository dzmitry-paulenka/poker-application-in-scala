import {Config} from 'app/config/Config';
import {rootStore} from 'app/store/RootStore';
import {action} from 'mobx';
import ReconnectingWebSocket from 'reconnecting-websocket';

export type EventListener<T> = (event: T) => void;

export class ConnectionController {
  private socket: ReconnectingWebSocket = null;
  private listenerMap: Map<string, Array<EventListener<any>>> = new Map();

  @action.bound
  public init() {
    const username = rootStore.username;
    const wsUrl = `${Config.websocketUrl()}/player-events/${username}`;

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
        this.send('pong', {});
        return;
      }

      const listeners = this.listenerMap.get(eventType) || [];
      for (let listener of listeners) {
        listener(event);
      }
    });
  }

  public listenEvents<T>(eventType: string, listener: EventListener<T>) {
    const listeners = this.listenerMap.get(eventType);
    if (!listeners)
      this.listenerMap.set(eventType, [listener]);
    else
      listeners.push(listener);
  }

  public send(eventType: string, data: any) {
    if (this.socket) {
      this.socket.send(JSON.stringify({
        ...data,
        eventType
      }));
    }
  }
}
