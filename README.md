![cobblemon repel neoreforged](https://cdn.modrinth.com/data/cached_images/5ed51d2067c95ff71b103ebb8e6dc46bec06f0ab.png) 

<br>Also you can download from [Modrinth.](https://modrinth.com/mod/cobblemon-repel-neoreforged) 
<br> (Thanks to the original author [deuli](https://modrinth.com/user/deuli) for this wonderful [mod](https://modrinth.com/mod/cobblemon-repel) on Fabric!) <br>

Cobblemon Repel NeoReforged is addon for [Cobblemon](https://www.example.com) that adds 3 new blocks that prevent Pokemon spawns in configurable radius.
<details>
<summary>Recipes</summary>

![Regular Repel Craft](https://cdn.modrinth.com/data/cached_images/8a28a4650950fb12b682472e17b316d02b947f82.png)
![Super Repel Craft](https://cdn.modrinth.com/data/cached_images/b29eda4ccc9eb941ddf1e902d2721e34c909ccd1.png)
![Max Repel Craft](https://cdn.modrinth.com/data/cached_images/26b37e38a96cecf07de596366429d69dbef4edbb.png)
</details>

### Changes
---
- parity with the original Fabric version, how it handled Filtering out Spawns.
- parity with the original Fabric version, such config used **/gamerule**,
``` 
now have gamerule for;
- RepelBaseRange = Base Range for all Repel blocks (default is 10)
- RepelSuperRangeMultiplier = This * Base Range (default is 2, output of 20)
- RepelMaxRangeMultipier = This * Base Range (default is 3, output of 30)
```
- changed block id (tbf idk why, kinda zonedout while doing it)
- ported changes from [hlam9's fork](https://github.com/hlam9/CobblemonRepel), **Player's Pokemon can spawn now within repel**
- **Pokesnacks spawn** are excluded by repel (intentional feature, i dont want to waste time waiting all day)
- **crouch+right-click** a repel will show a radius in particles. (will improve later,it works fine but radius is usually too large anyways to be seen)
- removed support for **Cobblemon Spawn Notification**

### how it works
---
1. subscribes to CobblemonEvents.POKEMON_ENTITY_SPAWN
2. checks get SpawnablePosition()
3. gathers variables from GetSpawner()
4. if the this event **isnt** cancelled, repel range is 0, PokeFishingSpawn or Spawned via Pokesnacks, proceed to Checking if Repel is Nearby
5. if Repel is Nearby then cancel spawn, error fallback is allowing the pokemon to spawn.
6. Mixin of ServerLevel is also there to cover Spawns outside the normal means of Cobblemon.(Pokebosses, etc.), this has ignore checks if it has UUID (usually player owned) and if it has crumb aspect (Pokesnack related)
