import {Injectable} from '@angular/core';
import {Item} from "./items/item";
import {ITEMS} from "./mock-items";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ItemService {

  constructor() {
    /* TODO document why this constructor is empty */
  }

  getItems(): Observable<Item[]> {
    return of(ITEMS);
  }
}
