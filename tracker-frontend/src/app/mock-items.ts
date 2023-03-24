import {Item} from "./items/item";
import {PriceHistory} from "./items/priceHistory";

const history: PriceHistory[] = [
  {
    "price": 1.57,
    "median": 1.64,
    "timestamp": "2023-03-22T21:19:29.522"
  },
  {
    "price": 1.72,
    "median": 1.65,
    "timestamp": "2023-03-23T20:53:55.155"
  },
  {
    "price": 1.7,
    "median": 1.65,
    "timestamp": "2023-03-23T21:00:00.293"
  },
  {
    "price": 2.22,
    "median": 2.23,
    "timestamp": "2023-03-24T22:40:21.711"
  }
]
export const ITEMS: Item[] = [
  {itemName: 'Chroma Case', priceHistory: history},
  {itemName: 'Chroma 2 Case', priceHistory: history},
  {itemName: 'Chroma 3 Case', priceHistory: history},
  {itemName: 'Clutch Case', priceHistory: history},
  {itemName: 'CS20 Case', priceHistory: history},
  {itemName: 'Danger Zone Case', priceHistory: history},
  {itemName: 'Falchion Case', priceHistory: history},
  {itemName: 'Gamma Case', priceHistory: history},
  {itemName: 'Gamma 2 Case', priceHistory: history},
  {itemName: 'Glove Case', priceHistory: history},
  {itemName: 'Horizon Case', priceHistory: history},
  {itemName: 'Huntsman Weapon Case', priceHistory: history},
  {itemName: 'Operation Breakout Weapon Case', priceHistory: history}
];
