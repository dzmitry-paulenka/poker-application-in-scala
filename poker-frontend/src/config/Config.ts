

export class Config {
  public static hostPort(): string {
    return "localhost:8080";
  }

  public static usersApiUrl(): string {
    return `http://${this.hostPort()}/api/users`;
  }

  public static websocketBaseUrl(): string {
    return `ws://${this.hostPort()}/api/events`;
  }
}