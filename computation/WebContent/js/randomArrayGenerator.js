function generateRandomArray(rangeFrom, rangeTo) {
			
			if(isNaN(rangeFrom) || isNaN(rangeTo)){
				rangeFrom = 2;
				rangeTo = 9;
			}
			if(rangeTo < rangeFrom){
				alert("RangeTo Should be greater than RangeFrom");
			}
			var rangeDiff = parseInt(rangeTo-rangeFrom);
			var array = new Array(rangeDiff+1);
			var randomCount = 0;
			for(var i = 0 ; i < 1000 ; i++) {
				var tempRandom = Math.floor((Math.random()*rangeTo)+1);
				if(tempRandom < rangeFrom || tempRandom > rangeTo) continue;
				
				var found = false;
				for (ii = 0; ii < array.length && !found; ii++) {
				  if (array[ii] == tempRandom) {
				    found = true; break;
				  }
				}
				if(!found) {
					array[randomCount++]  = tempRandom;	
				}
				if(randomCount == rangeDiff+1 ) {
					break;
				}
			}
			return array;
}