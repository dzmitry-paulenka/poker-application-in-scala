

export class Config {
  public static hostPort(): string {
    return "localhost:8080";
  }

  public static apiUrl(): string {
    return `http://${this.hostPort()}`;
  }

  public static websocketUrl(): string {
    return `ws://${this.hostPort()}`;
  }
}