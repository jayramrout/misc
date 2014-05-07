Drag and Drop the json file to the Application

{ "store": {
    "book": [ 
      { "category": "reference",
        "author": "Nigel Rees",
        "title": "Sayings of the Century",
        "price": 8.95
      },
      { "category": "fiction",
        "author": "Evelyn Waugh",
        "title": "Sword of Honour",
        "price": 12.99,
        "isbn": "0-553-21311-3"
      }
    ],
    "bicycle": {
      "color": "red",
      "price": 19.95
    }
  }
}

All authors: "$.store.book[*].author"
Author of first book in store: "$.store.book[1].author"
All books with category = "reference"
	"$.store.book[?(@.category == 'reference')]"
	JsonPath.read(json, "$.store.book[?]", filter(where("category").is("reference")));

All books that cost more than 10 USD:
	"$.store.book[?(@.price > 10)]"
	JsonPath.read(json, "$.store.book[?]", filter(where("price").gt(10)));

All books that have isbn:
	"$.store.book[?(@.isbn)]"
	JsonPath.read(json, "$.store.book[?]", filter(where("isbn").exists(true)));

All prices in the document:
	"$..price"
Chained filters:
	Filter filter = Filter.filter(Criteria.where("isbn").exists(true).and("category").in("fiction", "reference"))
	List<Object> books = JsonPath.read(json, "$.store.book[?]", filter);
Custom filters:
	Filter myFilter = new Filter.FilterAdapter<Map<String, Object>>(){
                @Override
                public boolean accept(Map<String, Object> map) {
                     return map.containsKey("isbn");   
                }
            };
	List<Object> books = JsonPath.read(json, "$.store.book[?]", myFilter);