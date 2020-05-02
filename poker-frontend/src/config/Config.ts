export class Config {
  public static isSecure(): boolean {
    return window.location.protocol == 'https';
  }

  public static hostPort(): string {
    if (window.location.port != '') {
      return 'localhost:8080';
    }

    const hostname = window.location.hostname;
    const tld = hostname.substring(hostname.indexOf('.') + 1);
    return `poker-api.${tld}`;
  }

  public static usersApiUrl(): string {
    const protocol = this.isSecure() ? 'https' : 'http';
    return `${protocol}://${this.hostPort()}/api/users`;
  }

  public static websocketBaseUrl(): string {
    const protocol = this.isSecure() ? 'wss' : 'ws';
    return `${protocol}://${this.hostPort()}/api/events`;
  }
}