import {Injectable} from '@angular/core';
import {Item} from "./items/item";
import {ITEMS} from "./mock-items";
import {Observable, of} from "rxjs";
import {MessageService} from "./message.service";

@Injectable({
  providedIn: 'root'
})
export class ItemService {

  constructor(private messageService: MessageService) {
  }

  getItems(): Observable<Item[]> {
    let item = of(ITEMS);
    this.messageService.add('ItemService: fetched items');
    return item;
  }

  getItem(id: number): Observable<Item> {
    const item = ITEMS.find(item => item.id === id)!;
    this.messageService.add(`ItemService: fetched item id=${id}`);
    return of(item);
  }
}
