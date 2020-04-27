export class Assert {
  public static yes(state: any, message: string = 'Invalid state'): void {
    if (!state) {
      console.error(`Assertion error: ${message}`);
      throw new Error(message);
    }
  }

  public static no(state: any, message: string = null): void {
    this.yes(!state, message);
  }
}