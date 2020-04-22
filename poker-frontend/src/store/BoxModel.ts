import {observable} from 'mobx';

let nextId = 1;

function generateId() {
  return nextId++;
}

export class BoxModel {
  readonly id: number;

  @observable
  public x: number;

  @observable
  public y: number;

  @observable
  public width: number;

  @observable
  public height: number;

  constructor(x: number, y: number, width: number = 100, height: number = 100) {
    this.id = generateId();
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
}

export default BoxModel;
