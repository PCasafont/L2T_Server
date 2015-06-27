/*
Copyright: L2 Tenkai ©
Description: Custom
*/
DROP TABLE IF EXISTS `custom_merchant_buylists`;
CREATE TABLE IF NOT EXISTS `custom_merchant_buylists` (
  `item_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `price` int(10) NOT NULL DEFAULT '0',
  `shop_id` mediumint(7) unsigned NOT NULL DEFAULT '0',
  `order` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `count` tinyint(2) NOT NULL DEFAULT '-1',
  `currentCount` tinyint(2) NOT NULL DEFAULT '-1',
  `time` int(11) NOT NULL DEFAULT '0',
  `savetimer` decimal(20,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`shop_id`,`order`)
);

INSERT INTO `custom_merchant_buylists` VALUES

-- Simple Swords
(1298,-1,90101,1,-1,-1,0,0), -- Caliburs
(1296,-1,90101,2,-1,-1,0,0), -- Gladius
(2,-1,90101,3,-1,-1,0,0), -- Long Sword
(120,-1,90101,4,-1,-1,0,0), -- Sword of Reflection
(121,-1,90101,6,-1,-1,0,0), -- Sword of Watershadow
(68,-1,90101,8,-1,-1,0,0), -- Falchion
(144,-1,90101,9,-1,-1,0,0), -- Sword of Occult
(143,-1,90101,10,-1,-1,0,0), -- Sword of Mystic
(128,-1,90101,11,-1,-1,0,0), -- Knight's Sword
(126,-1,90101,12,-1,-1,0,0), -- Artisan's Sword
(125,-1,90101,13,-1,-1,0,0), -- Spinebone Sword
(69,-1,90101,14,-1,-1,0,0), -- Bastard Sword
(127,-1,90101,15,-1,-1,0,0), -- Crimson Sword
(130,-1,90101,16,-1,-1,0,0), -- Elven Sword
(129,-1,90101,17,-1,-1,0,0), -- Sword of Revolution
(7886,-1,90101,18,-1,-1,0,0), -- Sword of Magic Fog
(2499,-1,90101,19,-1,-1,0,0), -- Elven Long Sword
(7887,-1,90101,20,-1,-1,0,0), -- Mysterious Sword
(72,-1,90101,21,-1,-1,0,0), -- Stormbringer
(84,-1,90101,22,-1,-1,0,0), -- Homunkulus's Sword
(145,-1,90101,23,-1,-1,0,0), -- Sword of Whispering Death
(74,-1,90101,24,-1,-1,0,0), -- Katana
(73,-1,90101,25,-1,-1,0,0), -- Shamshir
(133,-1,90101,26,-1,-1,0,0), -- Raid Sword
(131,-1,90101,27,-1,-1,0,0), -- Spirit Sword
(7888,-1,90101,28,-1,-1,0,0), -- Ecliptic Sword
(77,-1,90101,29,-1,-1,0,0), -- Tsurugi
(76,-1,90101,30,-1,-1,0,0), -- Sword of Delusion
(75,-1,90101,31,-1,-1,0,0), -- Caliburs
(132,-1,90101,32,-1,-1,0,0), -- Sword of Limit
(135,-1,90101,33,-1,-1,0,0), -- Samurai Longsword
(148,-1,90101,34,-1,-1,0,0), -- Sword of Valhalla
(146,-1,90101,35,-1,-1,0,0), -- Ghoulbane
(7889,-1,90101,36,-1,-1,0,0), -- Wizard's Tear
(138,-1,90101,37,-1,-1,0,0), -- (Not In Use)
(140,-1,90101,38,-1,-1,0,0), -- Shiny Agathion 7 Day Pack
(141,-1,90101,39,-1,-1,0,0), -- Sobbing Agathion 7 Day Pack
(142,-1,90101,40,-1,-1,0,0), -- Keshanberk
(137,-1,90101,41,-1,-1,0,0), -- (Not In Use)
(136,-1,90101,42,-1,-1,0,0), -- (Not In Use)
(79,-1,90101,43,-1,-1,0,0), -- Sword of Damascus
(150,-1,90101,44,-1,-1,0,0), -- Elemental Sword
(147,-1,90101,45,-1,-1,0,0), -- Tear of Darkness
(149,-1,90101,46,-1,-1,0,0), -- Sword of Life
(85,-1,90101,47,-1,-1,0,0), -- Phantom Sword
(80,-1,90101,48,-1,-1,0,0), -- Tallum Blade
(151,-1,90101,49,-1,-1,0,0), -- Sword of Miracles
(2500,-1,90101,50,-1,-1,0,0), -- Dark Legion's Edge
(8686,-1,90101,51,-1,-1,0,0), -- Themis' Tongue
(8678,-1,90101,52,-1,-1,0,0), -- Sirra's Blade
(82,-1,90101,53,-1,-1,0,0), -- God's Blade
(6364,-1,90101,54,-1,-1,0,0), -- Forgotten Blade
(9442,-1,90101,55,-1,-1,0,0), -- Dynasty Sword
(9444,-1,90101,56,-1,-1,0,0), -- Dynasty Phantom
(10215,-1,90101,57,-1,-1,0,0), -- Icarus Sawsword
(10217,-1,90101,58,-1,-1,0,0), -- Icarus Spirit
(13457,-1,90101,59,-1,-1,0,0), -- Vesper Cutter
(13459,-1,90101,60,-1,-1,0,0), -- Vesper Buster

-- Double Swords
(1333,-1,90102,1,-1,-1,0,0), -- Brandish
(3027,-1,90102,2,-1,-1,0,0), -- Old Knight's Sword
(5284,-1,90102,3,-1,-1,0,0), -- Zweihander
(5285,-1,90102,4,-1,-1,0,0), -- Heavy Sword
(7880,-1,90102,5,-1,-1,0,0), -- Steel Sword
(124,-1,90102,6,-1,-1,0,0), -- Two-Handed Sword
(7881,-1,90102,7,-1,-1,0,0), -- Titan Sword
(70,-1,90102,8,-1,-1,0,0), -- Claymore
(71,-1,90102,9,-1,-1,0,0), -- Flamberge
(7882,-1,90102,10,-1,-1,0,0), -- Pa'agrian Sword
(5286,-1,90102,11,-1,-1,0,0), -- Berserker Blade
(78,-1,90102,12,-1,-1,0,0), -- Great Sword
(7883,-1,90102,13,-1,-1,0,0), -- Guardian Sword
(7884,-1,90102,14,-1,-1,0,0), -- Infernal Master
(81,-1,90102,15,-1,-1,0,0), -- Dragon Slayer
(8679,-1,90102,16,-1,-1,0,0), -- Sword of Ipos
(6372,-1,90102,17,-1,-1,0,0), -- Heaven's Divider
(9443,-1,90102,18,-1,-1,0,0), -- Dynasty Blade
(10218,-1,90102,19,-1,-1,0,0), -- Icarus Heavy Arms
(13458,-1,90102,20,-1,-1,0,0), -- Vesper Slasher

-- 
(2516,-1,90103,1,-1,-1,0,0), -- Saber*Saber
(2519,-1,90103,2,-1,-1,0,0), -- Saber*Artisan's Sword
(2517,-1,90103,3,-1,-1,0,0), -- Saber*Bastard Sword
(2520,-1,90103,4,-1,-1,0,0), -- Saber*Knight's Sword
(2518,-1,90103,5,-1,-1,0,0), -- Saber*Spinebone Sword
(2540,-1,90103,6,-1,-1,0,0), -- Artisan's Sword*Artisan's Sword
(2541,-1,90103,7,-1,-1,0,0), -- Artisan's Sword*Knight's Sword
(2527,-1,90103,8,-1,-1,0,0), -- Bastard Sword*Artisan's Sword
(2525,-1,90103,9,-1,-1,0,0), -- Bastard Sword*Bastard Sword
(2528,-1,90103,10,-1,-1,0,0), -- Bastard Sword*Knight's Sword
(2526,-1,90103,11,-1,-1,0,0), -- Bastard Sword*Spinebone Sword
(2546,-1,90103,12,-1,-1,0,0), -- Knight's Sword*Knight's Sword
(2521,-1,90103,13,-1,-1,0,0), -- Saber*Crimson Sword
(2522,-1,90103,14,-1,-1,0,0), -- Saber*Elven Sword
(2534,-1,90103,15,-1,-1,0,0), -- Spinebone Sword*Artisan's Sword
(2535,-1,90103,16,-1,-1,0,0), -- Spinebone Sword*Knight's Sword
(2533,-1,90103,17,-1,-1,0,0), -- Spinebone Sword*Spinebone Sword
(2542,-1,90103,18,-1,-1,0,0), -- Artisan's Sword*Crimson Sword
(2543,-1,90103,19,-1,-1,0,0), -- Artisan's Sword*Elven Sword
(2529,-1,90103,20,-1,-1,0,0), -- Bastard Sword*Crimson Sword
(2530,-1,90103,21,-1,-1,0,0), -- Bastard Sword*Elven Sword
(2547,-1,90103,22,-1,-1,0,0), -- Knight's Sword*Crimson Sword
(2548,-1,90103,23,-1,-1,0,0), -- Knight's Sword*Elven Sword
(2536,-1,90103,24,-1,-1,0,0), -- Spinebone Sword*Crimson Sword
(2537,-1,90103,25,-1,-1,0,0), -- Spinebone Sword*Elven Sword
(2551,-1,90103,26,-1,-1,0,0), -- Crimson Sword*Crimson Sword
(2552,-1,90103,27,-1,-1,0,0), -- Crimson Sword*Elven Sword
(2555,-1,90103,28,-1,-1,0,0), -- Elven Sword*Elven Sword
(2523,-1,90103,29,-1,-1,0,0), -- Saber*Sword of Revolution
(2544,-1,90103,30,-1,-1,0,0), -- Artisan's Sword*Sword of Revolution
(2531,-1,90103,31,-1,-1,0,0), -- Bastard Sword*Sword of Revolution
(2549,-1,90103,32,-1,-1,0,0), -- Knight's Sword*Sword of Revolution
(2538,-1,90103,33,-1,-1,0,0), -- Spinebone Sword*Sword of Revolution
(2524,-1,90103,34,-1,-1,0,0), -- Saber*Elven Long Sword
(2532,-1,90103,35,-1,-1,0,0), -- Bastard Sword*Elven Long Sword
(2553,-1,90103,36,-1,-1,0,0), -- Crimson Sword*Sword of Revolution
(2556,-1,90103,37,-1,-1,0,0), -- Elven Sword*Sword of Revolution
(2550,-1,90103,38,-1,-1,0,0), -- Knight's Sword*Elven Long Sword
(2539,-1,90103,39,-1,-1,0,0), -- Spinebone Sword*Elven Long Sword
(2554,-1,90103,40,-1,-1,0,0), -- Crimson Sword*Elven Long Sword
(2557,-1,90103,41,-1,-1,0,0), -- Elven Sword*Elven Long Sword
(2558,-1,90103,42,-1,-1,0,0), -- Sword of Revolution*Sword of Revolution
(2559,-1,90103,43,-1,-1,0,0), -- Sword of Revolution*Elven Long Sword
(2560,-1,90103,44,-1,-1,0,0), -- Elven Long Sword*Elven Long Sword
(2561,-1,90103,45,-1,-1,0,0), -- Stormbringer*Stormbringer
(2563,-1,90103,46,-1,-1,0,0), -- Stormbringer*Katana
(2565,-1,90103,47,-1,-1,0,0), -- Stormbringer*Raid Sword
(2562,-1,90103,48,-1,-1,0,0), -- Stormbringer*Shamshir
(2564,-1,90103,49,-1,-1,0,0), -- Stormbringer*Spirit Sword
(2582,-1,90103,50,-1,-1,0,0), -- Katana*Katana
(2584,-1,90103,51,-1,-1,0,0), -- Katana*Raid Sword
(2583,-1,90103,52,-1,-1,0,0), -- Katana*Spirit Sword
(2599,-1,90103,53,-1,-1,0,0), -- Raid Sword*Raid Sword
(2573,-1,90103,54,-1,-1,0,0), -- Shamshir*Katana
(2575,-1,90103,55,-1,-1,0,0), -- Shamshir*Raid Sword
(2572,-1,90103,56,-1,-1,0,0), -- Shamshir*Shamshir
(2574,-1,90103,57,-1,-1,0,0), -- Shamshir*Spirit Sword
(2592,-1,90103,58,-1,-1,0,0), -- Spirit Sword*Raid Sword
(2591,-1,90103,59,-1,-1,0,0), -- Spirit Sword*Spirit Sword
(2566,-1,90103,60,-1,-1,0,0), -- Stormbringer*Caliburs
(2568,-1,90103,61,-1,-1,0,0), -- Stormbringer*Sword of Delusion
(2567,-1,90103,62,-1,-1,0,0), -- Stormbringer*Sword of Limit
(2569,-1,90103,63,-1,-1,0,0), -- Stormbringer*Sword of Nightmare
(2570,-1,90103,64,-1,-1,0,0), -- Stormbringer*Tsurugi
(2585,-1,90103,65,-1,-1,0,0), -- Katana*Caliburs
(2587,-1,90103,66,-1,-1,0,0), -- Katana*Sword of Delusion
(2586,-1,90103,67,-1,-1,0,0), -- Katana*Sword of Limit
(2588,-1,90103,68,-1,-1,0,0), -- Katana*Sword of Nightmare
(2589,-1,90103,69,-1,-1,0,0), -- Katana*Tsurugi
(2600,-1,90103,70,-1,-1,0,0), -- Raid Sword*Caliburs
(2602,-1,90103,71,-1,-1,0,0), -- Raid Sword*Sword of Delusion
(2601,-1,90103,72,-1,-1,0,0), -- Raid Sword*Sword of Limit
(2603,-1,90103,73,-1,-1,0,0), -- Raid Sword*Sword of Nightmare
(2604,-1,90103,74,-1,-1,0,0), -- Raid Sword*Tsurugi
(2576,-1,90103,75,-1,-1,0,0), -- Shamshir*Caliburs
(2578,-1,90103,76,-1,-1,0,0), -- Shamshir*Sword of Delusion
(2577,-1,90103,77,-1,-1,0,0), -- Shamshir*Sword of Limit
(2579,-1,90103,78,-1,-1,0,0), -- Shamshir*Sword of Nightmare
(2580,-1,90103,79,-1,-1,0,0), -- Shamshir*Tsurugi
(2593,-1,90103,80,-1,-1,0,0), -- Spirit Sword*Caliburs
(2595,-1,90103,81,-1,-1,0,0), -- Spirit Sword*Sword of Delusion
(2594,-1,90103,82,-1,-1,0,0), -- Spirit Sword*Sword of Limit
(2596,-1,90103,83,-1,-1,0,0), -- Spirit Sword*Sword of Nightmare
(2597,-1,90103,84,-1,-1,0,0), -- Spirit Sword*Tsurugi
(2606,-1,90103,85,-1,-1,0,0), -- Caliburs*Caliburs
(2608,-1,90103,86,-1,-1,0,0), -- Caliburs*Sword of Delusion
(2607,-1,90103,87,-1,-1,0,0), -- Caliburs*Sword of Limit
(2609,-1,90103,88,-1,-1,0,0), -- Caliburs*Sword of Nightmare
(2610,-1,90103,89,-1,-1,0,0), -- Caliburs*Tsurugi
(2571,-1,90103,90,-1,-1,0,0), -- Stormbringer*Samurai Long sword
(2617,-1,90103,91,-1,-1,0,0), -- Sword of Delusion*Sword of Delusion
(2618,-1,90103,92,-1,-1,0,0), -- Sword of Delusion*Sword of Nightmare
(2619,-1,90103,93,-1,-1,0,0), -- Sword of Delusion*Tsurugi
(2613,-1,90103,94,-1,-1,0,0), -- Sword of Limit*Sword of Delusion
(2612,-1,90103,95,-1,-1,0,0), -- Sword of Limit*Sword of Limit
(2614,-1,90103,96,-1,-1,0,0), -- Sword of Limit*Sword of Nightmare
(2615,-1,90103,97,-1,-1,0,0), -- Sword of Limit*Tsurugi
(2622,-1,90103,98,-1,-1,0,0), -- Sword of Nightmare*Tsurugi
(2624,-1,90103,99,-1,-1,0,0), -- Tsurugi*Tsurugi
(2590,-1,90103,100,-1,-1,0,0), -- Katana*Samurai Long Sword
(2605,-1,90103,101,-1,-1,0,0), -- Raid Sword*Samurai Long Sword
(2581,-1,90103,102,-1,-1,0,0), -- Shamshir*Samurai Long Sword
(2598,-1,90103,103,-1,-1,0,0), -- Spirit Sword*Samurai Long Sword
(2611,-1,90103,104,-1,-1,0,0), -- Caliburs*Samurai Long Sword
(2620,-1,90103,105,-1,-1,0,0), -- Sword of Delusion*Samurai Long Sword
(2616,-1,90103,106,-1,-1,0,0), -- Sword of Limit*Samurai Long Sword
(2623,-1,90103,107,-1,-1,0,0), -- Sword of Nightmare*Samurai Long Sword
(2625,-1,90103,108,-1,-1,0,0), -- Tsurugi*Samurai Long Sword
(2626,-1,90103,109,-1,-1,0,0), -- Samurai Long Sword*Samurai Long Sword
(5233,-1,90103,110,-1,-1,0,0), -- Keshanberk*Keshanberk
(5704,-1,90103,111,-1,-1,0,0), -- Keshanberk*Keshanberk
(5705,-1,90103,112,-1,-1,0,0), -- Keshanberk*Damascus
(5706,-1,90103,113,-1,-1,0,0), -- Damascus*Damascus
(8938,-1,90103,114,-1,-1,0,0), -- Damascus * Tallum Blade
(6580,-1,90103,115,-1,-1,0,0), -- Tallum Blade*Dark Legion's Edge
(10004,-1,90103,116,-1,-1,0,0), -- Dynasty Dual Sword
(10415,-1,90103,117,-1,-1,0,0), -- Icarus Dual Sword
(52,-1,90103,118,-1,-1,0,0), -- Vesper Dual Sword

-- 
(10,-1,90104,1,-1,-1,0,0), -- Dagger
(11,-1,90104,2,-1,-1,0,0), -- Bone Dagger
(215,-1,90104,3,-1,-1,0,0), -- Doom Dagger
(12,-1,90104,4,-1,-1,0,0), -- Knife
(2372,-1,90104,5,-1,-1,0,0), -- Dagger of Adept
(989,-1,90104,6,-1,-1,0,0), -- Eldritch Dagger
(216,-1,90104,7,-1,-1,0,0), -- Dirk
(946,-1,90104,9,-1,-1,0,0), -- Skeleton Dagger
(217,-1,90104,10,-1,-1,0,0), -- Shining Knife
(218,-1,90104,11,-1,-1,0,0), -- Throwing Knife
(219,-1,90104,13,-1,-1,0,0), -- Sword Breaker
(221,-1,90104,14,-1,-1,0,0), -- Assassin Knife
(220,-1,90104,15,-1,-1,0,0), -- Crafted Dagger
(222,-1,90104,16,-1,-1,0,0), -- Poniard Dagger
(240,-1,90104,17,-1,-1,0,0), -- Conjurer's Knife
(238,-1,90104,18,-1,-1,0,0), -- Dagger of Mana
(241,-1,90104,19,-1,-1,0,0), -- Shilen Knife
(223,-1,90104,20,-1,-1,0,0), -- Kukuri
(1660,-1,90104,21,-1,-1,0,0), -- Cursed Maingauche
(225,-1,90104,22,-1,-1,0,0), -- Mithril Dagger
(226,-1,90104,23,-1,-1,0,0), -- Cursed Dagger
(232,-1,90104,24,-1,-1,0,0), -- Dark Elven Dagger
(230,-1,90104,25,-1,-1,0,0), -- Wolverine Needle
(242,-1,90104,26,-1,-1,0,0), -- Soulfire Dirk
(227,-1,90104,27,-1,-1,0,0), -- Stiletto
(233,-1,90104,28,-1,-1,0,0), -- Dark Screamer
(231,-1,90104,29,-1,-1,0,0), -- Grace Dagger
(228,-1,90104,30,-1,-1,0,0), -- Crystal Dagger
(243,-1,90104,31,-1,-1,0,0), -- Hell Knife
(229,-1,90104,32,-1,-1,0,0), -- Kris
(234,-1,90104,33,-1,-1,0,0), -- Demon Dagger
(235,-1,90104,34,-1,-1,0,0), -- Bloody Orchid
(236,-1,90104,35,-1,-1,0,0), -- Soul Separator
(8682,-1,90104,36,-1,-1,0,0), -- Naga Storm
(237,-1,90104,37,-1,-1,0,0), -- Dragon's Tooth
(6367,-1,90104,38,-1,-1,0,0), -- Angel Slayer
(9446,-1,90104,39,-1,-1,0,0), -- Dynasty Knife
(10216,-1,90104,40,-1,-1,0,0), -- Icarus Disperser
(13460,-1,90104,41,-1,-1,0,0), -- Vesper Shaper
(13884,-1,90104,42,-1,-1,0,0), -- Vesper Dual Daggers

-- 
(13,-1,90105,1,-1,-1,0,0), -- Short Bow
(14,-1,90105,2,-1,-1,0,0), -- Bow
(3028,-1,90105,3,-1,-1,0,0), -- Crescent Moon Bow
(271,-1,90105,4,-1,-1,0,0), -- Hunting Bow
(272,-1,90105,6,-1,-1,0,0), -- Forest Bow
(273,-1,90105,7,-1,-1,0,0), -- Composite Bow
(274,-1,90105,8,-1,-1,0,0), -- Strengthened Bow
(277,-1,90105,9,-1,-1,0,0), -- Dark Elven Bow
(276,-1,90105,10,-1,-1,0,0), -- Elven Bow
(275,-1,90105,13,-1,-1,0,0), -- Long Bow
(278,-1,90105,14,-1,-1,0,0), -- Gastraphetes
(279,-1,90105,15,-1,-1,0,0), -- Strengthened Long Bow
(280,-1,90105,16,-1,-1,0,0), -- Light Crossbow
(281,-1,90105,17,-1,-1,0,0), -- Crystallized Ice Bow
(285,-1,90105,18,-1,-1,0,0), -- Noble Elven Bow
(282,-1,90105,19,-1,-1,0,0), -- Elemental Bow
(283,-1,90105,20,-1,-1,0,0), -- Akat Long Bow
(286,-1,90105,21,-1,-1,0,0), -- Eminence Bow
(284,-1,90105,22,-1,-1,0,0), -- Dark Elven Long Bow
(287,-1,90105,23,-1,-1,0,0), -- Bow of Peril
(288,-1,90105,24,-1,-1,0,0), -- Carnage Bow
(289,-1,90105,25,-1,-1,0,0), -- Soul Bow
(8684,-1,90105,26,-1,-1,0,0), -- Shyeed's Bow
(290,-1,90105,27,-1,-1,0,0), -- The Bow
(7575,-1,90105,28,-1,-1,0,0), -- Draconic Bow
(6368,-1,90105,29,-1,-1,0,0), -- Shining Bow
(17,-1,90105,30,-1,-1,0,0), -- Wooden Arrow
(1341,-1,90105,31,-1,-1,0,0), -- Bone Arrow
(1342,-1,90105,32,-1,-1,0,0), -- Iron Arrow
(1343,-1,90105,33,-1,-1,0,0), -- Silver Arrow
(1344,-1,90105,34,-1,-1,0,0), -- Mithril Arrow
(1345,-1,90105,35,-1,-1,0,0), -- Shining Arrow
(9633,-1,90105,36,-1,-1,0,0), -- Bone Bolt
(9634,-1,90105,37,-1,-1,0,0), -- Steel Bolt
(9445,-1,90105,38,-1,-1,0,0), -- Dynasty Bow
(10223,-1,90105,39,-1,-1,0,0), -- Icarus Spitter
(13467,-1,90105,40,-1,-1,0,0), -- Vesper Thrower

-- 
(253,-1,90106,1,-1,-1,0,0), -- Spiked Gloves
(254,-1,90106,2,-1,-1,0,0), -- Iron Gloves
(2371,-1,90106,3,-1,-1,0,0), -- Fist of Butcher
(255,-1,90106,4,-1,-1,0,0), -- Fox Claw Gloves
(256,-1,90106,5,-1,-1,0,0), -- Cestus
(257,-1,90106,6,-1,-1,0,0), -- Viper Fang
(258,-1,90106,7,-1,-1,0,0), -- Bagh-Nakh
(259,-1,90106,8,-1,-1,0,0), -- Single-Edged Jamadhr
(260,-1,90106,10,-1,-1,0,0), -- Triple-Edged Jamadhr
(261,-1,90106,11,-1,-1,0,0), -- Bich'Hwa
(262,-1,90106,12,-1,-1,0,0), -- Scallop Jamadhr
(263,-1,90106,13,-1,-1,0,0), -- Chakram
(4233,-1,90106,14,-1,-1,0,0), -- Knuckle Duster
(265,-1,90106,15,-1,-1,0,0), -- Fisted Blade
(266,-1,90106,16,-1,-1,0,0), -- Great Pata
(264,-1,90106,17,-1,-1,0,0), -- Pata
(267,-1,90106,18,-1,-1,0,0), -- Arthro Nail
(268,-1,90106,19,-1,-1,0,0), -- Bellion Cestus
(269,-1,90106,20,-1,-1,0,0), -- Blood Tornado
(270,-1,90106,21,-1,-1,0,0), -- Dragon Grinder
(8685,-1,90106,22,-1,-1,0,0), -- Sobekk's Hurricane
(6371,-1,90106,23,-1,-1,0,0), -- Demon Splinter
(9450,-1,90106,24,-1,-1,0,0), -- Dynasty Bagh-Nakh
(10221,-1,90106,25,-1,-1,0,0), -- Icarus Hand
(13461,-1,90106,26,-1,-1,0,0), -- Vesper Fighter

-- 
(1302,-1,90107,1,-1,-1,0,0), -- Bec de Corbin
(15,-1,90107,2,-1,-1,0,0), -- Short Spear
(3026,-1,90107,3,-1,-1,0,0), -- Talins Spear
(16,-1,90107,4,-1,-1,0,0), -- Great Spear
(291,-1,90107,5,-1,-1,0,0), -- Trident
(1472,-1,90107,6,-1,-1,0,0), -- Dreadbane
(1376,-1,90107,7,-1,-1,0,0), -- Guard Spear
(295,-1,90107,8,-1,-1,0,0), -- Dwarven Trident
(292,-1,90107,9,-1,-1,0,0), -- Pike
(296,-1,90107,12,-1,-1,0,0), -- Dwarven Pike
(293,-1,90107,13,-1,-1,0,0), -- War Hammer
(7896,-1,90107,14,-1,-1,0,0), -- Titan Hammer
(93,-1,90107,15,-1,-1,0,0), -- Winged Spear
(187,-1,90107,16,-1,-1,0,0), -- Atuba Hammer
(190,-1,90107,17,-1,-1,0,0), -- Atuba Mace
(297,-1,90107,18,-1,-1,0,0), -- Glaive
(194,-1,90107,19,-1,-1,0,0), -- Heavy Doom Axe
(191,-1,90107,20,-1,-1,0,0), -- Heavy Doom Hammer
(302,-1,90107,21,-1,-1,0,0), -- Body Slasher
(298,-1,90107,22,-1,-1,0,0), -- Orcish Glaive
(96,-1,90107,23,-1,-1,0,0), -- Scythe
(94,-1,90107,24,-1,-1,0,0), -- Bec de Corbin
(199,-1,90107,25,-1,-1,0,0), -- Pa'agrian Hammer
(95,-1,90107,26,-1,-1,0,0), -- Poleaxe
(7898,-1,90107,27,-1,-1,0,0), -- Karik Horn
(203,-1,90107,28,-1,-1,0,0), -- Pa'agrian Axe
(301,-1,90107,29,-1,-1,0,0), -- Scorpion
(303,-1,90107,30,-1,-1,0,0), -- Widow Maker
(299,-1,90107,31,-1,-1,0,0), -- Orcish Poleaxe
(7897,-1,90107,32,-1,-1,0,0), -- Dwarven Hammer
(300,-1,90107,33,-1,-1,0,0), -- Great Axe
(7900,-1,90107,34,-1,-1,0,0), -- Ice Storm Hammer
(97,-1,90107,35,-1,-1,0,0), -- Lance
(7901,-1,90107,36,-1,-1,0,0), -- Star Buster
(98,-1,90107,37,-1,-1,0,0), -- Halberd
(304,-1,90107,38,-1,-1,0,0), -- Orcish Halberd
(305,-1,90107,39,-1,-1,0,0), -- Tallum Glaive
(8683,-1,90107,40,-1,-1,0,0), -- Tiphon's Spear
(7899,-1,90107,41,-1,-1,0,0), -- Destroyer Hammer
(7902,-1,90107,42,-1,-1,0,0), -- Doom Crusher
(8681,-1,90107,43,-1,-1,0,0), -- Behemoth's Tuning Fork
(306,-1,90107,44,-1,-1,0,0), -- Dragon Claw Axe
(6370,-1,90107,46,-1,-1,0,0), -- SaINTSpear
(6369,-1,90107,47,-1,-1,0,0), -- Dragon Hunter Axe
(9447,-1,90107,48,-1,-1,0,0), -- Dynasty Halberd
(10253,-1,90107,49,-1,-1,0,0), -- Dynasty Crusher
(10219,-1,90107,50,-1,-1,0,0), -- Icarus Trident
(10220,-1,90107,51,-1,-1,0,0), -- Icarus Hammer
(13462,-1,90107,52,-1,-1,0,0), -- Vesper Stormer
(13463,-1,90107,53,-1,-1,0,0), -- Vesper Avenger

-- 
(4,-1,90108,1,-1,-1,0,0), -- Club
(152,-1,90108,2,-1,-1,0,0), -- Heavy Chisel
(5,-1,90108,3,-1,-1,0,0), -- Mace
(747,-1,90108,4,-1,-1,0,0), -- Wand of Adept
(153,-1,90108,5,-1,-1,0,0), -- Sickle
(1511,-1,90108,6,-1,-1,0,0), -- Silversmith Hammer
(154,-1,90108,7,-1,-1,0,0), -- Dwarven Mace
(1300,-1,90108,8,-1,-1,0,0), -- Apprentice's Rod
(1301,-1,90108,9,-1,-1,0,0), -- Big Hammer
(2501,-1,90108,10,-1,-1,0,0), -- Bone Club
(155,-1,90108,11,-1,-1,0,0), -- Flanged Mace
(87,-1,90108,12,-1,-1,0,0), -- Iron Hammer
(156,-1,90108,14,-1,-1,0,0), -- Hand Axe
(166,-1,90108,15,-1,-1,0,0), -- Heavy Mace
(167,-1,90108,16,-1,-1,0,0), -- Scalpel
(168,-1,90108,17,-1,-1,0,0), -- Work Hammer
(182,-1,90108,18,-1,-1,0,0), -- Doom Hammer
(180,-1,90108,19,-1,-1,0,0), -- Mace of Judgment
(181,-1,90108,20,-1,-1,0,0), -- Mace of Miracle
(179,-1,90108,21,-1,-1,0,0), -- Mace of Prayer
(86,-1,90108,22,-1,-1,0,0), -- Tomahawk
(7890,-1,90108,23,-1,-1,0,0), -- Priest Mace
(157,-1,90108,24,-1,-1,0,0), -- Spiked Club
(172,-1,90108,25,-1,-1,0,0), -- Heavy Bone Club
(88,-1,90108,26,-1,-1,0,0), -- Morning Star
(169,-1,90108,27,-1,-1,0,0), -- Skull Breaker
(158,-1,90108,28,-1,-1,0,0), -- Tarbar
(159,-1,90108,29,-1,-1,0,0), -- Bonebreaker
(193,-1,90108,30,-1,-1,0,0), -- Stick of Faith
(160,-1,90108,31,-1,-1,0,0), -- Battle Axe
(89,-1,90108,32,-1,-1,0,0), -- Big Hammer
(161,-1,90108,33,-1,-1,0,0), -- Silver Axe
(173,-1,90108,34,-1,-1,0,0), -- Skull Graver
(2502,-1,90108,35,-1,-1,0,0), -- Dwarven War Hammer
(201,-1,90108,36,-1,-1,0,0), -- Club of Nature
(202,-1,90108,37,-1,-1,0,0), -- Mace of the Underworld
(174,-1,90108,38,-1,-1,0,0), -- Nirvana Axe
(196,-1,90108,39,-1,-1,0,0), -- Stick of Eternity
(162,-1,90108,40,-1,-1,0,0), -- War Axe
(7891,-1,90108,41,-1,-1,0,0), -- Ecliptic Axe
(2503,-1,90108,42,-1,-1,0,0), -- Yaksa Mace
(7892,-1,90108,43,-1,-1,0,0), -- Spell Breaker
(91,-1,90108,44,-1,-1,0,0), -- Heavy War Axe
(7893,-1,90108,45,-1,-1,0,0), -- Kaim Vanul's Bones
(175,-1,90108,46,-1,-1,0,0), -- Art of Battle Axe
(171,-1,90108,47,-1,-1,0,0), -- Deadman's Glory
(7894,-1,90108,48,-1,-1,0,0), -- Spiritual Eye
(2504,-1,90108,49,-1,-1,0,0), -- Meteor Shower
(8687,-1,90108,50,-1,-1,0,0), -- Cabrio's Hand
(7895,-1,90108,51,-1,-1,0,0), -- Flaming Dragon Skull
(164,-1,90108,52,-1,-1,0,0), -- Elysian
(8680,-1,90108,53,-1,-1,0,0), -- Barakiel's Axe
(6579,-1,90108,54,-1,-1,0,0), -- Arcana Mace
(165,-1,90108,55,-1,-1,0,0), -- Yablonski's Hammer
(6365,-1,90108,56,-1,-1,0,0), -- Basalt Battlehammer
(9448,-1,90108,57,-1,-1,0,0), -- Dynasty Cudgel
(10222,-1,90108,58,-1,-1,0,0), -- Icarus Hall
(13464,-1,90108,59,-1,-1,0,0), -- Vesper Retributer
(13466,-1,90108,60,-1,-1,0,0), -- Vesper Singer

-- 
(99,-1,90109,1,-1,-1,0,0), -- Apprentice's Spellbook
(8,-1,90109,2,-1,-1,0,0), -- Willow Staff
(2373,-1,90109,3,-1,-1,0,0), -- Eldritch Staff
(754,-1,90109,4,-1,-1,0,0), -- Red Sunset Staff
(744,-1,90109,5,-1,-1,0,0), -- Staff of Sentinel
(1304,-1,90109,6,-1,-1,0,0), -- Conjuror's Staff
(9,-1,90109,7,-1,-1,0,0), -- Cedar Staff
(310,-1,90109,8,-1,-1,0,0), -- Relic of the Saints
(309,-1,90109,9,-1,-1,0,0), -- Tears of Eva
(176,-1,90109,10,-1,-1,0,0), -- Journeyman's Staff
(311,-1,90109,11,-1,-1,0,0), -- Crucifix of Blessing
(100,-1,90109,12,-1,-1,0,0), -- Voodoo Doll
(177,-1,90109,13,-1,-1,0,0), -- Mage Staff
(312,-1,90109,14,-1,-1,0,0), -- Branch of Life
(314,-1,90109,15,-1,-1,0,0), -- Proof of Revenge
(101,-1,90109,16,-1,-1,0,0), -- Scroll of Wisdom
(313,-1,90109,17,-1,-1,0,0), -- Temptation of Abyss
(178,-1,90109,18,-1,-1,0,0), -- Bone Staff
(315,-1,90109,19,-1,-1,0,0), -- Divine Tome
(184,-1,90109,21,-1,-1,0,0), -- Conjuror's Staff
(183,-1,90109,22,-1,-1,0,0), -- Mystic Staff
(185,-1,90109,23,-1,-1,0,0), -- Staff of Mana
(316,-1,90109,24,-1,-1,0,0), -- Blood of Saints
(317,-1,90109,25,-1,-1,0,0), -- Tome of Blood
(186,-1,90109,26,-1,-1,0,0), -- Staff of Magic
(318,-1,90109,27,-1,-1,0,0), -- Crucifix of Blood
(319,-1,90109,28,-1,-1,0,0), -- Eye of Infinity
(320,-1,90109,29,-1,-1,0,0), -- Blue Crystal Skull
(321,-1,90109,30,-1,-1,0,0), -- Demon Fangs
(323,-1,90109,31,-1,-1,0,0), -- Ancient Reagent
(189,-1,90109,32,-1,-1,0,0), -- Staff of Life
(322,-1,90109,33,-1,-1,0,0), -- Vajra Wands
(90,-1,90109,34,-1,-1,0,0), -- Goat Head Staff
(188,-1,90109,35,-1,-1,0,0), -- Ghost Staff
(325,-1,90109,36,-1,-1,0,0), -- Horn of Glory
(324,-1,90109,37,-1,-1,0,0), -- Tears of Fairy
(192,-1,90109,38,-1,-1,0,0), -- Crystal Staff
(326,-1,90109,39,-1,-1,0,0), -- Heathen's Book
(327,-1,90109,40,-1,-1,0,0), -- Hex Doll
(195,-1,90109,41,-1,-1,0,0), -- Cursed Staff
(329,-1,90109,42,-1,-1,0,0), -- Blessed Branch
(328,-1,90109,43,-1,-1,0,0), -- Candle of Wisdom
(331,-1,90109,44,-1,-1,0,0), -- Cerberus Eye
(333,-1,90109,45,-1,-1,0,0), -- Claws of Black Dragon
(330,-1,90109,46,-1,-1,0,0), -- Phoenix Feather
(332,-1,90109,47,-1,-1,0,0), -- Scroll of Destruction
(334,-1,90109,48,-1,-1,0,0), -- Three Eyed Crow's Feather
(198,-1,90109,49,-1,-1,0,0), -- Inferno Staff
(197,-1,90109,50,-1,-1,0,0), -- Paradia Staff
(200,-1,90109,51,-1,-1,0,0), -- Sage's Staff
(204,-1,90109,52,-1,-1,0,0), -- Deadman's Staff
(206,-1,90109,53,-1,-1,0,0), -- Demon's Staff
(205,-1,90109,54,-1,-1,0,0), -- Ghoul's Staff
(335,-1,90109,55,-1,-1,0,0), -- Soul Crystal
(339,-1,90109,56,-1,-1,0,0), -- Blood Crystal
(336,-1,90109,57,-1,-1,0,0), -- Scroll of Mana
(337,-1,90109,58,-1,-1,0,0), -- Scroll of Massacre
(92,-1,90109,59,-1,-1,0,0), -- Sprite's Staff
(207,-1,90109,60,-1,-1,0,0), -- Staff of Phantom
(208,-1,90109,61,-1,-1,0,0), -- Staff of Seal
(340,-1,90109,62,-1,-1,0,0), -- Unicorn's Horn
(338,-1,90109,63,-1,-1,0,0), -- Wyvern's Skull
(209,-1,90109,64,-1,-1,0,0), -- Divine Staff
(210,-1,90109,65,-1,-1,0,0), -- Staff of Evil Spirits
(211,-1,90109,66,-1,-1,0,0), -- Staff of Nobility
(345,-1,90109,67,-1,-1,0,0), -- Deathbringer Sword
(342,-1,90109,68,-1,-1,0,0), -- Enchanted Flute
(341,-1,90109,69,-1,-1,0,0), -- Forgotten Tome
(343,-1,90109,70,-1,-1,0,0), -- Headless Arrow
(344,-1,90109,71,-1,-1,0,0), -- Proof of Overlord
(212,-1,90109,72,-1,-1,0,0), -- Dasparion's Staff
(213,-1,90109,73,-1,-1,0,0), -- Branch of the Mother Tree
(8688,-1,90109,74,-1,-1,0,0), -- Daimon Crystal
(346,-1,90109,75,-1,-1,0,0), -- Tears of Fallen Angel
(214,-1,90109,76,-1,-1,0,0), -- The Staff
(6366,-1,90109,77,-1,-1,0,0), -- Imperial Staff
(9449,-1,90109,78,-1,-1,0,0), -- Dynasty Mace
(10252,-1,90109,79,-1,-1,0,0), -- Dynasty Staff
(13465,-1,90109,80,-1,-1,0,0), -- Vesper Caster

-- Simple Swords
(8111,-1,90121,1,-1,-1,0,0), -- Mysterious Sword
(8112,-1,90121,2,-1,-1,0,0), -- Mysterious Sword
(8113,-1,90121,3,-1,-1,0,0), -- Mysterious Sword
(4681,-1,90121,4,-1,-1,0,0), -- Stormbringer
(4682,-1,90121,5,-1,-1,0,0), -- Stormbringer
(4683,-1,90121,6,-1,-1,0,0), -- Stormbringer
(6313,-1,90121,7,-1,-1,0,0), -- Homunkulus's Sword
(6314,-1,90121,8,-1,-1,0,0), -- Homunkulus's Sword
(6315,-1,90121,9,-1,-1,0,0), -- Homunkulus's Sword
(6310,-1,90121,10,-1,-1,0,0), -- Sword of Whispering Death
(6311,-1,90121,11,-1,-1,0,0), -- Sword of Whispering Death
(6312,-1,90121,12,-1,-1,0,0), -- Sword of Whispering Death
(4687,-1,90121,13,-1,-1,0,0), -- Katana
(4688,-1,90121,14,-1,-1,0,0), -- Katana
(4689,-1,90121,15,-1,-1,0,0), -- Katana
(4693,-1,90121,16,-1,-1,0,0), -- Raid Sword
(4694,-1,90121,17,-1,-1,0,0), -- Raid Sword
(4695,-1,90121,18,-1,-1,0,0), -- Raid Sword
(4684,-1,90121,19,-1,-1,0,0), -- Shamshir
(4685,-1,90121,20,-1,-1,0,0), -- Shamshir
(4686,-1,90121,21,-1,-1,0,0), -- Shamshir
(4690,-1,90121,22,-1,-1,0,0), -- Spirit Sword
(4691,-1,90121,23,-1,-1,0,0), -- Spirit Sword
(4692,-1,90121,24,-1,-1,0,0), -- Spirit Sword
(8114,-1,90121,25,-1,-1,0,0), -- Ecliptic Sword
(8115,-1,90121,26,-1,-1,0,0), -- Ecliptic Sword
(8116,-1,90121,27,-1,-1,0,0), -- Ecliptic Sword
(4696,-1,90121,28,-1,-1,0,0), -- Caliburs
(4697,-1,90121,29,-1,-1,0,0), -- Caliburs
(4698,-1,90121,30,-1,-1,0,0), -- Caliburs
(4699,-1,90121,31,-1,-1,0,0), -- Sword of Delusion
(4700,-1,90121,32,-1,-1,0,0), -- Sword of Delusion
(4701,-1,90121,33,-1,-1,0,0), -- Sword of Delusion
(6307,-1,90121,34,-1,-1,0,0), -- Sword of Limit
(6308,-1,90121,35,-1,-1,0,0), -- Sword of Limit
(6309,-1,90121,36,-1,-1,0,0), -- Sword of Limit
(4706,-1,90121,37,-1,-1,0,0), -- Sword of Nightmare
(4705,-1,90121,38,-1,-1,0,0), -- Sword of Nightmare
(4707,-1,90121,39,-1,-1,0,0), -- Sword of Nightmare
(4702,-1,90121,40,-1,-1,0,0), -- Tsurugi
(4703,-1,90121,41,-1,-1,0,0), -- Tsurugi
(4704,-1,90121,42,-1,-1,0,0), -- Tsurugi
(4709,-1,90121,43,-1,-1,0,0), -- Samurai Longsword
(4708,-1,90121,44,-1,-1,0,0), -- Samurai Longsword
(4710,-1,90121,45,-1,-1,0,0), -- Samurai Longsword
(7722,-1,90121,46,-1,-1,0,0), -- Sword of Valhalla
(7723,-1,90121,47,-1,-1,0,0), -- Sword of Valhalla
(7724,-1,90121,48,-1,-1,0,0), -- Sword of Valhalla
(8117,-1,90121,49,-1,-1,0,0), -- Wizard's Tear
(8118,-1,90121,50,-1,-1,0,0), -- Wizard's Tear
(8119,-1,90121,51,-1,-1,0,0), -- Wizard's Tear
(4716,-1,90121,52,-1,-1,0,0), -- Keshanberk
(4715,-1,90121,53,-1,-1,0,0), -- Keshanberk
(4714,-1,90121,54,-1,-1,0,0), -- Keshanberk
(4718,-1,90121,55,-1,-1,0,0), -- Sword of Damascus
(4717,-1,90121,56,-1,-1,0,0), -- Sword of Damascus
(4719,-1,90121,57,-1,-1,0,0), -- Sword of Damascus
(5640,-1,90121,58,-1,-1,0,0), -- Elemental Sword
(5639,-1,90121,59,-1,-1,0,0), -- Elemental Sword
(5638,-1,90121,60,-1,-1,0,0), -- Elemental Sword
(5643,-1,90121,61,-1,-1,0,0), -- Sword of Miracles
(5642,-1,90121,62,-1,-1,0,0), -- Sword of Miracles
(5641,-1,90121,63,-1,-1,0,0), -- Sword of Miracles
(8814,-1,90121,64,-1,-1,0,0), -- Themis' Tongue
(8813,-1,90121,65,-1,-1,0,0), -- Themis' Tongue
(8812,-1,90121,66,-1,-1,0,0), -- Themis' Tongue
(5637,-1,90121,67,-1,-1,0,0), -- Tallum Blade
(5635,-1,90121,68,-1,-1,0,0), -- Tallum Blade
(5636,-1,90121,69,-1,-1,0,0), -- Tallum Blade
(4720,-1,90121,70,-1,-1,0,0), -- Tallum Blade
(4721,-1,90121,71,-1,-1,0,0), -- Tallum Blade
(4722,-1,90121,72,-1,-1,0,0), -- Tallum Blade
(5647,-1,90121,73,-1,-1,0,0), -- Dark Legion's Edge
(5648,-1,90121,74,-1,-1,0,0), -- Dark Legion's Edge
(5649,-1,90121,75,-1,-1,0,0), -- Dark Legion's Edge
(8790,-1,90121,76,-1,-1,0,0), -- Sirra's Blade
(8788,-1,90121,77,-1,-1,0,0), -- Sirra's Blade
(8789,-1,90121,78,-1,-1,0,0), -- Sirra's Blade
(6581,-1,90121,79,-1,-1,0,0), -- Forgotten Blade
(6582,-1,90121,80,-1,-1,0,0), -- Forgotten Blade
(6583,-1,90121,81,-1,-1,0,0), -- Forgotten Blade
(9854,-1,90121,82,-1,-1,0,0), -- Dynasty Sword
(9855,-1,90121,83,-1,-1,0,0), -- Dynasty Sword
(9856,-1,90121,84,-1,-1,0,0), -- Dynasty Sword
(9860,-1,90121,85,-1,-1,0,0), -- Dynasty Phantom
(9861,-1,90121,86,-1,-1,0,0), -- Dynasty Phantom
(9862,-1,90121,87,-1,-1,0,0), -- Dynasty Phantom
(10434,-1,90121,88,-1,-1,0,0), -- Icarus Sawsword
(10435,-1,90121,89,-1,-1,0,0), -- Icarus Sawsword
(10436,-1,90121,90,-1,-1,0,0), -- Icarus Sawsword
(10440,-1,90121,91,-1,-1,0,0), -- Icarus Spirit
(10441,-1,90121,92,-1,-1,0,0), -- Icarus Spirit
(10442,-1,90121,93,-1,-1,0,0), -- Icarus Spirit
(14118,-1,90121,94,-1,-1,0,0), -- Vesper Cutter
(14119,-1,90121,95,-1,-1,0,0), -- Vesper Cutter
(14120,-1,90121,96,-1,-1,0,0), -- Vesper Cutter
(14124,-1,90121,97,-1,-1,0,0), -- Vesper Buster
(14125,-1,90121,98,-1,-1,0,0), -- Vesper Buster
(14126,-1,90121,99,-1,-1,0,0), -- Vesper Buster

-- Double Swords
(4711,-1,90122,1,-1,-1,0,0), -- Flamberge
(4712,-1,90122,2,-1,-1,0,0), -- Flamberge
(4713,-1,90122,3,-1,-1,0,0), -- Flamberge
(8102,-1,90122,4,-1,-1,0,0), -- Pa'agrian Sword
(8103,-1,90122,5,-1,-1,0,0), -- Pa'agrian Sword
(8104,-1,90122,6,-1,-1,0,0), -- Pa'agrian Sword
(6347,-1,90122,7,-1,-1,0,0), -- Berserker Blade
(6348,-1,90122,8,-1,-1,0,0), -- Berserker Blade
(6349,-1,90122,9,-1,-1,0,0), -- Berserker Blade
(4723,-1,90122,10,-1,-1,0,0), -- Great Sword
(4724,-1,90122,11,-1,-1,0,0), -- Great Sword
(4725,-1,90122,12,-1,-1,0,0), -- Great Sword
(8105,-1,90122,13,-1,-1,0,0), -- Guardian Sword
(8106,-1,90122,14,-1,-1,0,0), -- Guardian Sword
(8107,-1,90122,15,-1,-1,0,0), -- Guardian Sword
(8108,-1,90122,16,-1,-1,0,0), -- Infernal Master
(8109,-1,90122,17,-1,-1,0,0), -- Infernal Master
(8110,-1,90122,18,-1,-1,0,0), -- Infernal Master
(5644,-1,90122,19,-1,-1,0,0), -- Dragon Slayer
(5645,-1,90122,20,-1,-1,0,0), -- Dragon Slayer
(5646,-1,90122,21,-1,-1,0,0), -- Dragon Slayer
(9760,-1,90122,22,-1,-1,0,0), -- Orkurus' Recommendation
(9761,-1,90122,23,-1,-1,0,0), -- Frozen Cell Samples
(9762,-1,90122,24,-1,-1,0,0), -- Orders
(8791,-1,90122,25,-1,-1,0,0), -- Sword of Ipos
(8792,-1,90122,26,-1,-1,0,0), -- Sword of Ipos
(8793,-1,90122,27,-1,-1,0,0), -- Sword of Ipos
(6605,-1,90122,28,-1,-1,0,0), -- Heavens Divider
(6606,-1,90122,29,-1,-1,0,0), -- Heavens Divider
(6607,-1,90122,30,-1,-1,0,0), -- Heavens Divider
(9857,-1,90122,31,-1,-1,0,0), -- Dynasty Blade
(9858,-1,90122,32,-1,-1,0,0), -- Dynasty Blade
(9859,-1,90122,33,-1,-1,0,0), -- Dynasty Blade
(10437,-1,90122,34,-1,-1,0,0), -- Icarus Heavy Arms
(10438,-1,90122,35,-1,-1,0,0), -- Icarus Heavy Arms
(10439,-1,90122,36,-1,-1,0,0), -- Icarus Heavy Arms
(14121,-1,90122,37,-1,-1,0,0), -- Vesper Slasher
(14122,-1,90122,38,-1,-1,0,0), -- Vesper Slasher
(14123,-1,90122,39,-1,-1,0,0), -- Vesper Slasher

-- 
(7811,-1,90124,1,-1,-1,0,0), -- Soulfire Dirk
(7812,-1,90124,2,-1,-1,0,0), -- Soulfire Dirk
(7810,-1,90124,3,-1,-1,0,0), -- Soulfire Dirk
(4759,-1,90124,4,-1,-1,0,0), -- Cursed Dagger
(4760,-1,90124,5,-1,-1,0,0), -- Cursed Dagger
(4761,-1,90124,6,-1,-1,0,0), -- Cursed Dagger
(4763,-1,90124,7,-1,-1,0,0), -- Dark Elven Dagger
(4762,-1,90124,8,-1,-1,0,0), -- Dark Elven Dagger
(4764,-1,90124,9,-1,-1,0,0), -- Dark Elven Dagger
(6356,-1,90124,10,-1,-1,0,0), -- Dark Elven Dagger
(4765,-1,90124,11,-1,-1,0,0), -- Stiletto
(4766,-1,90124,12,-1,-1,0,0), -- Stiletto
(4767,-1,90124,13,-1,-1,0,0), -- Stiletto
(6357,-1,90124,14,-1,-1,0,0), -- Stiletto
(4773,-1,90124,15,-1,-1,0,0), -- Dark Screamer
(4771,-1,90124,16,-1,-1,0,0), -- Dark Screamer
(4772,-1,90124,17,-1,-1,0,0), -- Dark Screamer
(4770,-1,90124,18,-1,-1,0,0), -- Grace Dagger
(4768,-1,90124,19,-1,-1,0,0), -- Grace Dagger
(4769,-1,90124,20,-1,-1,0,0), -- Grace Dagger
(4774,-1,90124,21,-1,-1,0,0), -- Crystal Dagger
(6358,-1,90124,22,-1,-1,0,0), -- Crystal Dagger
(4775,-1,90124,23,-1,-1,0,0), -- Crystal Dagger
(4776,-1,90124,24,-1,-1,0,0), -- Crystal Dagger
(4787,-1,90124,25,-1,-1,0,0), -- Hell Knife
(4786,-1,90124,26,-1,-1,0,0), -- Hell Knife
(7813,-1,90124,27,-1,-1,0,0), -- Hell Knife
(7815,-1,90124,28,-1,-1,0,0), -- Hell Knife
(7814,-1,90124,29,-1,-1,0,0), -- Hell Knife
(4788,-1,90124,30,-1,-1,0,0), -- Hell Knife
(4779,-1,90124,31,-1,-1,0,0), -- Kris
(4777,-1,90124,32,-1,-1,0,0), -- Kris
(4778,-1,90124,33,-1,-1,0,0), -- Kris
(4780,-1,90124,34,-1,-1,0,0), -- Demon Dagger
(6359,-1,90124,35,-1,-1,0,0), -- Demon Dagger
(4781,-1,90124,36,-1,-1,0,0), -- Demon Dagger
(4782,-1,90124,37,-1,-1,0,0), -- Demon Dagger
(5615,-1,90124,38,-1,-1,0,0), -- Bloody Orchid
(4785,-1,90124,39,-1,-1,0,0), -- Bloody Orchid
(5616,-1,90124,40,-1,-1,0,0), -- Bloody Orchid
(4783,-1,90124,41,-1,-1,0,0), -- Bloody Orchid
(5614,-1,90124,42,-1,-1,0,0), -- Bloody Orchid
(4784,-1,90124,43,-1,-1,0,0), -- Bloody Orchid
(5618,-1,90124,44,-1,-1,0,0), -- Soul Separator
(5617,-1,90124,45,-1,-1,0,0), -- Soul Separator
(5619,-1,90124,46,-1,-1,0,0), -- Soul Separator
(8802,-1,90124,47,-1,-1,0,0), -- Naga Storm
(8801,-1,90124,48,-1,-1,0,0), -- Naga Storm
(8800,-1,90124,49,-1,-1,0,0), -- Naga Storm
(6590,-1,90124,50,-1,-1,0,0), -- Angel Slayer
(6592,-1,90124,51,-1,-1,0,0), -- Angel Slayer
(6591,-1,90124,52,-1,-1,0,0), -- Angel Slayer
(9768,-1,90124,53,-1,-1,0,0), -- Dark Elves' Reply
(9769,-1,90124,54,-1,-1,0,0), -- Report to Sione
(9770,-1,90124,55,-1,-1,0,0), -- Empty Soul Crystal
(9866,-1,90124,56,-1,-1,0,0), -- Dynasty Knife
(9867,-1,90124,57,-1,-1,0,0), -- Dynasty Knife
(9868,-1,90124,58,-1,-1,0,0), -- Dynasty Knife
(10446,-1,90124,59,-1,-1,0,0), -- Icarus Disperser
(10447,-1,90124,60,-1,-1,0,0), -- Icarus Disperser
(10448,-1,90124,61,-1,-1,0,0), -- Icarus Disperser
(14127,-1,90124,62,-1,-1,0,0), -- Vesper Shaper
(14128,-1,90124,63,-1,-1,0,0), -- Vesper Shaper
(14129,-1,90124,64,-1,-1,0,0), -- Vesper Shaper
(13884,-1,90124,65,-1,-1,0,0), -- Vesper Dual Daggers

-- Bows - Arrows
(4811,-1,90125,1,-1,-1,0,0), -- Crystallized Ice Bow
(4810,-1,90125,2,-1,-1,0,0), -- Crystallized Ice Bow
(4812,-1,90125,3,-1,-1,0,0), -- Crystallized Ice Bow
(4818,-1,90125,4,-1,-1,0,0), -- Elven Bow of Nobility
(4816,-1,90125,5,-1,-1,0,0), -- Elven Bow of Nobility
(4817,-1,90125,6,-1,-1,0,0), -- Elven Bow of Nobility
(4813,-1,90125,7,-1,-1,0,0), -- Elemental Bow
(4814,-1,90125,8,-1,-1,0,0), -- Elemental Bow
(4815,-1,90125,9,-1,-1,0,0), -- Elemental Bow
(4820,-1,90125,10,-1,-1,0,0), -- Akat Long Bow
(4819,-1,90125,11,-1,-1,0,0), -- Akat Long Bow
(4821,-1,90125,12,-1,-1,0,0), -- Akat Long Bow
(4824,-1,90125,13,-1,-1,0,0), -- Eminence Bow
(4822,-1,90125,14,-1,-1,0,0), -- Eminence Bow
(4823,-1,90125,15,-1,-1,0,0), -- Eminence Bow
(4826,-1,90125,16,-1,-1,0,0), -- Dark Elven Long Bow
(4825,-1,90125,17,-1,-1,0,0), -- Dark Elven Long Bow
(4827,-1,90125,18,-1,-1,0,0), -- Dark Elven Long Bow
(4830,-1,90125,19,-1,-1,0,0), -- Bow of Peril
(4828,-1,90125,20,-1,-1,0,0), -- Bow of Peril
(4829,-1,90125,21,-1,-1,0,0), -- Bow of Peril
(4831,-1,90125,22,-1,-1,0,0), -- Carnage Bow
(5609,-1,90125,23,-1,-1,0,0), -- Carnage Bow
(5608,-1,90125,24,-1,-1,0,0), -- Carnage Bow
(4832,-1,90125,25,-1,-1,0,0), -- Carnage Bow
(5610,-1,90125,26,-1,-1,0,0), -- Carnage Bow
(4833,-1,90125,27,-1,-1,0,0), -- Carnage Bow
(5611,-1,90125,28,-1,-1,0,0), -- Soul Bow
(5613,-1,90125,29,-1,-1,0,0), -- Soul Bow
(5612,-1,90125,30,-1,-1,0,0), -- Soul Bow
(8806,-1,90125,31,-1,-1,0,0), -- Shyeed's Bow
(8807,-1,90125,32,-1,-1,0,0), -- Shyeed's Bow
(8808,-1,90125,33,-1,-1,0,0), -- Shyeed's Bow
(7576,-1,90125,34,-1,-1,0,0), -- Draconic Bow
(7578,-1,90125,35,-1,-1,0,0), -- Draconic Bow
(7577,-1,90125,36,-1,-1,0,0), -- Draconic Bow
(6593,-1,90125,37,-1,-1,0,0), -- Shining Bow
(6595,-1,90125,38,-1,-1,0,0), -- Shining Bow
(6594,-1,90125,39,-1,-1,0,0), -- Shining Bow
(9756,-1,90125,40,-1,-1,0,0), -- Report - East
(9757,-1,90125,41,-1,-1,0,0), -- Report - North
(9758,-1,90125,42,-1,-1,0,0), -- Harkilgamed's Letter
(9863,-1,90125,43,-1,-1,0,0), -- Dynasty Bow
(9864,-1,90125,44,-1,-1,0,0), -- Dynasty Bow
(9865,-1,90125,45,-1,-1,0,0), -- Dynasty Bow
(10443,-1,90125,46,-1,-1,0,0), -- Icarus Spitter
(10444,-1,90125,47,-1,-1,0,0), -- Icarus Spitter
(10445,-1,90125,48,-1,-1,0,0), -- Icarus Spitter
(14148,-1,90125,49,-1,-1,0,0), -- Vesper Thrower
(14149,-1,90125,50,-1,-1,0,0), -- Vesper Thrower
(14150,-1,90125,51,-1,-1,0,0), -- Vesper Thrower
(17,-1,90125,52,-1,-1,0,0), -- Wooden Arrow
(1341,-1,90125,53,-1,-1,0,0), -- Bone Arrow
(1342,-1,90125,54,-1,-1,0,0), -- Iron Arrow
(1343,-1,90125,55,-1,-1,0,0), -- Silver Arrow
(1344,-1,90125,56,-1,-1,0,0), -- Mithril Arrow
(1345,-1,90125,57,-1,-1,0,0), -- Shining Arrow
(9633,-1,90125,58,-1,-1,0,0), -- Bone Bolt
(9634,-1,90125,59,-1,-1,0,0), -- Steel Bolt
(9635,-1,90125,60,-1,-1,0,0), -- Silver Bolt
(9636,-1,90125,61,-1,-1,0,0), -- Mithril Bolt
(9637,-1,90125,62,-1,-1,0,0), -- Shining Bolt

-- 
(4789,-1,90126,1,-1,-1,0,0), -- Chakram
(4790,-1,90126,2,-1,-1,0,0), -- Chakram
(4791,-1,90126,3,-1,-1,0,0), -- Chakram
(4800,-1,90126,4,-1,-1,0,0), -- Knuckle Duster
(4798,-1,90126,5,-1,-1,0,0), -- Knuckle Duster
(4799,-1,90126,6,-1,-1,0,0), -- Knuckle Duster
(4794,-1,90126,7,-1,-1,0,0), -- Fisted Blade
(4792,-1,90126,8,-1,-1,0,0), -- Fisted Blade
(4793,-1,90126,9,-1,-1,0,0), -- Fisted Blade
(4795,-1,90126,10,-1,-1,0,0), -- Great Pata
(4796,-1,90126,11,-1,-1,0,0), -- Great Pata
(4797,-1,90126,12,-1,-1,0,0), -- Great Pata
(4801,-1,90126,13,-1,-1,0,0), -- Arthro Nail
(4802,-1,90126,14,-1,-1,0,0), -- Arthro Nail
(4803,-1,90126,15,-1,-1,0,0), -- Arthro Nail
(4804,-1,90126,16,-1,-1,0,0), -- Bellion Cestus
(4805,-1,90126,17,-1,-1,0,0), -- Bellion Cestus
(4806,-1,90126,18,-1,-1,0,0), -- Bellion Cestus
(5622,-1,90126,19,-1,-1,0,0), -- Blood Tornado
(4807,-1,90126,20,-1,-1,0,0), -- Blood Tornado
(5621,-1,90126,21,-1,-1,0,0), -- Blood Tornado
(5620,-1,90126,22,-1,-1,0,0), -- Blood Tornado
(4809,-1,90126,23,-1,-1,0,0), -- Blood Tornado
(4808,-1,90126,24,-1,-1,0,0), -- Blood Tornado
(5624,-1,90126,25,-1,-1,0,0), -- Dragon Grinder
(5625,-1,90126,26,-1,-1,0,0), -- Dragon Grinder
(5623,-1,90126,27,-1,-1,0,0), -- Dragon Grinder
(9771,-1,90126,28,-1,-1,0,0), -- Tak's Captured Soul
(9772,-1,90126,29,-1,-1,0,0), -- Steelrazor Evaluation
(9773,-1,90126,30,-1,-1,0,0), -- Enmity Crystal
(8811,-1,90126,31,-1,-1,0,0), -- Sobekk's Hurricane
(8810,-1,90126,32,-1,-1,0,0), -- Sobekk's Hurricane
(8809,-1,90126,33,-1,-1,0,0), -- Sobekk's Hurricane
(6604,-1,90126,34,-1,-1,0,0), -- Demon Splinter
(6602,-1,90126,35,-1,-1,0,0), -- Demon Splinter
(6603,-1,90126,36,-1,-1,0,0), -- Demon Splinter
(9878,-1,90126,37,-1,-1,0,0), -- Dynasty Bagh-Nakh
(9879,-1,90126,38,-1,-1,0,0), -- Dynasty Bagh-Nakh
(9880,-1,90126,39,-1,-1,0,0), -- Dynasty Bagh-Nakh
(10458,-1,90126,40,-1,-1,0,0), -- Icarus Hand
(10459,-1,90126,41,-1,-1,0,0), -- Icarus Hand
(10460,-1,90126,42,-1,-1,0,0), -- Icarus Hand
(14130,-1,90126,43,-1,-1,0,0), -- Vesper Fighter
(14131,-1,90126,44,-1,-1,0,0), -- Vesper Fighter
(14132,-1,90126,45,-1,-1,0,0), -- Vesper Fighter

-- 
(4872,-1,90127,1,-1,-1,0,0), -- Heavy Doom Axe
(4870,-1,90127,2,-1,-1,0,0), -- Heavy Doom Axe
(4871,-1,90127,3,-1,-1,0,0), -- Heavy Doom Axe
(4866,-1,90127,4,-1,-1,0,0), -- Heavy Doom Hammer
(4864,-1,90127,5,-1,-1,0,0), -- Heavy Doom Hammer
(4865,-1,90127,6,-1,-1,0,0), -- Heavy Doom Hammer
(4840,-1,90127,7,-1,-1,0,0), -- Body Slasher
(4841,-1,90127,8,-1,-1,0,0), -- Body Slasher
(4842,-1,90127,9,-1,-1,0,0), -- Body Slasher
(4837,-1,90127,10,-1,-1,0,0), -- Orcish Glaive
(4838,-1,90127,11,-1,-1,0,0), -- Orcish Glaive
(4839,-1,90127,12,-1,-1,0,0), -- Orcish Glaive
(4834,-1,90127,13,-1,-1,0,0), -- Scythe
(4835,-1,90127,14,-1,-1,0,0), -- Scythe
(4836,-1,90127,15,-1,-1,0,0), -- Scythe
(4843,-1,90127,16,-1,-1,0,0), -- Bec de Corbin
(4845,-1,90127,17,-1,-1,0,0), -- Bec de Corbin
(4844,-1,90127,18,-1,-1,0,0), -- Bec de Corbin
(4880,-1,90127,19,-1,-1,0,0), -- Pa'agrian Hammer
(4881,-1,90127,20,-1,-1,0,0), -- Pa'agrian Hammer
(4879,-1,90127,21,-1,-1,0,0), -- Pa'agrian Hammer
(7719,-1,90127,22,-1,-1,0,0), -- Poleaxe
(7720,-1,90127,23,-1,-1,0,0), -- Poleaxe
(7721,-1,90127,24,-1,-1,0,0), -- Poleaxe
(4887,-1,90127,25,-1,-1,0,0), -- Pa'agrian Axe
(4886,-1,90127,26,-1,-1,0,0), -- Pa'agrian Axe
(4885,-1,90127,27,-1,-1,0,0), -- Pa'agrian Axe
(4846,-1,90127,28,-1,-1,0,0), -- Scorpion
(4847,-1,90127,29,-1,-1,0,0), -- Scorpion
(4848,-1,90127,30,-1,-1,0,0), -- Scorpion
(4849,-1,90127,31,-1,-1,0,0), -- Widow Maker
(4850,-1,90127,32,-1,-1,0,0), -- Widow Maker
(4851,-1,90127,33,-1,-1,0,0), -- Widow Maker
(4852,-1,90127,34,-1,-1,0,0), -- Orcish Poleaxe
(4853,-1,90127,35,-1,-1,0,0), -- Orcish Poleaxe
(4854,-1,90127,36,-1,-1,0,0), -- Orcish Poleaxe
(8125,-1,90127,37,-1,-1,0,0), -- Karik Horn
(8123,-1,90127,38,-1,-1,0,0), -- Karik Horn
(8124,-1,90127,39,-1,-1,0,0), -- Karik Horn
(8121,-1,90127,40,-1,-1,0,0), -- Dwarven Hammer
(8122,-1,90127,41,-1,-1,0,0), -- Dwarven Hammer
(8120,-1,90127,42,-1,-1,0,0), -- Dwarven Hammer
(4855,-1,90127,43,-1,-1,0,0), -- Great Axe
(4856,-1,90127,44,-1,-1,0,0), -- Great Axe
(4857,-1,90127,45,-1,-1,0,0), -- Great Axe
(4858,-1,90127,46,-1,-1,0,0), -- Lance
(4859,-1,90127,47,-1,-1,0,0), -- Lance
(4860,-1,90127,48,-1,-1,0,0), -- Lance
(8130,-1,90127,49,-1,-1,0,0), -- Ice Storm Hammer
(8131,-1,90127,50,-1,-1,0,0), -- Ice Storm Hammer
(8129,-1,90127,51,-1,-1,0,0), -- Ice Storm Hammer
(8133,-1,90127,52,-1,-1,0,0), -- Star Buster
(8132,-1,90127,53,-1,-1,0,0), -- Star Buster
(8134,-1,90127,54,-1,-1,0,0), -- Star Buster
(5627,-1,90127,55,-1,-1,0,0), -- Halberd
(4861,-1,90127,56,-1,-1,0,0), -- Halberd
(5626,-1,90127,57,-1,-1,0,0), -- Halberd
(4862,-1,90127,58,-1,-1,0,0), -- Halberd
(4863,-1,90127,59,-1,-1,0,0), -- Halberd
(5628,-1,90127,60,-1,-1,0,0), -- Halberd
(5629,-1,90127,61,-1,-1,0,0), -- Orcish Halberd
(5630,-1,90127,62,-1,-1,0,0), -- Orcish Halberd
(5631,-1,90127,63,-1,-1,0,0), -- Orcish Halberd
(5632,-1,90127,64,-1,-1,0,0), -- Tallum Glaive
(5633,-1,90127,65,-1,-1,0,0), -- Tallum Glaive
(5634,-1,90127,66,-1,-1,0,0), -- Tallum Glaive
(8803,-1,90127,67,-1,-1,0,0), -- Tiphon's Spear
(8804,-1,90127,68,-1,-1,0,0), -- Tiphon's Spear
(8805,-1,90127,69,-1,-1,0,0), -- Tiphon's Spear
(8128,-1,90127,70,-1,-1,0,0), -- Destroyer Hammer
(8127,-1,90127,71,-1,-1,0,0), -- Destroyer Hammer
(8126,-1,90127,72,-1,-1,0,0), -- Destroyer Hammer
(8136,-1,90127,73,-1,-1,0,0), -- Doom Crusher
(8135,-1,90127,74,-1,-1,0,0), -- Doom Crusher
(8137,-1,90127,75,-1,-1,0,0), -- Doom Crusher
(8799,-1,90127,76,-1,-1,0,0), -- Behemoth's Tuning Fork
(8797,-1,90127,77,-1,-1,0,0), -- Behemoth's Tuning Fork
(8798,-1,90127,78,-1,-1,0,0), -- Behemoth's Tuning Fork
(6600,-1,90127,79,-1,-1,0,0), -- SaINTSpear
(6601,-1,90127,80,-1,-1,0,0), -- SaINTSpear
(6599,-1,90127,81,-1,-1,0,0), -- SaINTSpear
(6597,-1,90127,82,-1,-1,0,0), -- Dragon Hunter Axe
(6598,-1,90127,83,-1,-1,0,0), -- Dragon Hunter Axe
(6596,-1,90127,84,-1,-1,0,0), -- Dragon Hunter Axe
(9869,-1,90127,85,-1,-1,0,0), -- Dynasty Halberd
(9870,-1,90127,86,-1,-1,0,0), -- Dynasty Halberd
(9871,-1,90127,87,-1,-1,0,0), -- Dynasty Halberd
(10449,-1,90127,88,-1,-1,0,0), -- Icarus Trident
(10450,-1,90127,89,-1,-1,0,0), -- Icarus Trident
(10451,-1,90127,90,-1,-1,0,0), -- Icarus Trident
(10452,-1,90127,91,-1,-1,0,0), -- Icarus Hammer
(10453,-1,90127,92,-1,-1,0,0), -- Icarus Hammer
(10454,-1,90127,93,-1,-1,0,0), -- Icarus Hammer
(14133,-1,90127,94,-1,-1,0,0), -- Vesper Stormer
(14134,-1,90127,95,-1,-1,0,0), -- Vesper Stormer
(14135,-1,90127,96,-1,-1,0,0), -- Vesper Stormer
(14136,-1,90127,97,-1,-1,0,0), -- Vesper Avenger
(14137,-1,90127,98,-1,-1,0,0), -- Vesper Avenger
(14138,-1,90127,99,-1,-1,0,0), -- Vesper Avenger

-- 
(7702,-1,90128,1,-1,-1,0,0), -- Stick of Faith
(7703,-1,90128,2,-1,-1,0,0), -- Stick of Faith
(7701,-1,90128,3,-1,-1,0,0), -- Stick of Faith
(4729,-1,90128,4,-1,-1,0,0), -- Battle Axe
(4731,-1,90128,5,-1,-1,0,0), -- Battle Axe
(4730,-1,90128,6,-1,-1,0,0), -- Battle Axe
(4728,-1,90128,7,-1,-1,0,0), -- Big Hammer
(4726,-1,90128,8,-1,-1,0,0), -- Big Hammer
(4727,-1,90128,9,-1,-1,0,0), -- Big Hammer
(4732,-1,90128,10,-1,-1,0,0), -- Silver Axe
(4734,-1,90128,11,-1,-1,0,0), -- Silver Axe
(4733,-1,90128,12,-1,-1,0,0), -- Silver Axe
(4735,-1,90128,13,-1,-1,0,0), -- Skull Graver
(4736,-1,90128,14,-1,-1,0,0), -- Skull Graver
(4737,-1,90128,15,-1,-1,0,0), -- Skull Graver
(7710,-1,90128,16,-1,-1,0,0), -- Club of Nature
(7712,-1,90128,17,-1,-1,0,0), -- Club of Nature
(7711,-1,90128,18,-1,-1,0,0), -- Club of Nature
(7715,-1,90128,19,-1,-1,0,0), -- Mace of the Underworld
(7714,-1,90128,20,-1,-1,0,0), -- Mace of the Underworld
(7713,-1,90128,21,-1,-1,0,0), -- Mace of the Underworld
(7708,-1,90128,22,-1,-1,0,0), -- Nirvana Axe
(7707,-1,90128,23,-1,-1,0,0), -- Nirvana Axe
(7709,-1,90128,24,-1,-1,0,0), -- Nirvana Axe
(7706,-1,90128,25,-1,-1,0,0), -- Stick of Eternity
(7704,-1,90128,26,-1,-1,0,0), -- Stick of Eternity
(7705,-1,90128,27,-1,-1,0,0), -- Stick of Eternity
(4738,-1,90128,28,-1,-1,0,0), -- Dwarven War Hammer
(4740,-1,90128,29,-1,-1,0,0), -- Dwarven War Hammer
(4739,-1,90128,30,-1,-1,0,0), -- Dwarven War Hammer
(8138,-1,90128,31,-1,-1,0,0), -- Ecliptic Axe
(8140,-1,90128,32,-1,-1,0,0), -- Ecliptic Axe
(8139,-1,90128,33,-1,-1,0,0), -- Ecliptic Axe
(4741,-1,90128,34,-1,-1,0,0), -- War Axe
(4743,-1,90128,35,-1,-1,0,0), -- War Axe
(4742,-1,90128,36,-1,-1,0,0), -- War Axe
(4744,-1,90128,37,-1,-1,0,0), -- Yaksa Mace
(4745,-1,90128,38,-1,-1,0,0), -- Yaksa Mace
(4746,-1,90128,39,-1,-1,0,0), -- Yaksa Mace
(8141,-1,90128,40,-1,-1,0,0), -- Spell Breaker
(8143,-1,90128,41,-1,-1,0,0), -- Spell Breaker
(8142,-1,90128,42,-1,-1,0,0), -- Spell Breaker
(8146,-1,90128,43,-1,-1,0,0), -- Kaim Vanul's Bones
(8145,-1,90128,44,-1,-1,0,0), -- Kaim Vanul's Bones
(8144,-1,90128,45,-1,-1,0,0), -- Kaim Vanul's Bones
(4747,-1,90128,46,-1,-1,0,0), -- Heavy War Axe
(4748,-1,90128,47,-1,-1,0,0), -- Heavy War Axe
(4749,-1,90128,48,-1,-1,0,0), -- Heavy War Axe
(4755,-1,90128,49,-1,-1,0,0), -- Art of Battle Axe
(4753,-1,90128,50,-1,-1,0,0), -- Art of Battle Axe
(4754,-1,90128,51,-1,-1,0,0), -- Art of Battle Axe
(4750,-1,90128,52,-1,-1,0,0), -- Deadman's Glory
(4752,-1,90128,53,-1,-1,0,0), -- Deadman's Glory
(4751,-1,90128,54,-1,-1,0,0), -- Deadman's Glory
(8149,-1,90128,55,-1,-1,0,0), -- Spiritual Eye
(8148,-1,90128,56,-1,-1,0,0), -- Spiritual Eye
(8147,-1,90128,57,-1,-1,0,0), -- Spiritual Eye
(8150,-1,90128,58,-1,-1,0,0), -- Flaming Dragon Skull
(8151,-1,90128,59,-1,-1,0,0), -- Flaming Dragon Skull
(8152,-1,90128,60,-1,-1,0,0), -- Flaming Dragon Skull
(8815,-1,90128,61,-1,-1,0,0), -- Cabrio's Hand
(8817,-1,90128,62,-1,-1,0,0), -- Cabrio's Hand
(8816,-1,90128,63,-1,-1,0,0), -- Cabrio's Hand
(5600,-1,90128,64,-1,-1,0,0), -- Meteor Shower
(4757,-1,90128,65,-1,-1,0,0), -- Meteor Shower
(5599,-1,90128,66,-1,-1,0,0), -- Meteor Shower
(4756,-1,90128,67,-1,-1,0,0), -- Meteor Shower
(4758,-1,90128,68,-1,-1,0,0), -- Meteor Shower
(5601,-1,90128,69,-1,-1,0,0), -- Meteor Shower
(5603,-1,90128,70,-1,-1,0,0), -- Elysian
(5604,-1,90128,71,-1,-1,0,0), -- Elysian
(5602,-1,90128,72,-1,-1,0,0), -- Elysian
(8796,-1,90128,73,-1,-1,0,0), -- Barakiel's Axe
(8795,-1,90128,74,-1,-1,0,0), -- Barakiel's Axe
(8794,-1,90128,75,-1,-1,0,0), -- Barakiel's Axe
(6608,-1,90128,76,-1,-1,0,0), -- Arcana Mace
(6610,-1,90128,77,-1,-1,0,0), -- Arcana Mace
(6609,-1,90128,78,-1,-1,0,0), -- Arcana Mace
(6585,-1,90128,79,-1,-1,0,0), -- Basalt Battlehammer
(6584,-1,90128,80,-1,-1,0,0), -- Basalt Battlehammer
(6586,-1,90128,81,-1,-1,0,0), -- Basalt Battlehammer
(9872,-1,90128,82,-1,-1,0,0), -- Dynasty Cudgel
(9873,-1,90128,83,-1,-1,0,0), -- Dynasty Cudgel
(9874,-1,90128,84,-1,-1,0,0), -- Dynasty Cudgel
(10455,-1,90128,85,-1,-1,0,0), -- Icarus Hall
(10456,-1,90128,86,-1,-1,0,0), -- Icarus Hall
(10457,-1,90128,87,-1,-1,0,0), -- Icarus Hall
(14145,-1,90128,88,-1,-1,0,0), -- Vesper Singer
(14146,-1,90128,89,-1,-1,0,0), -- Vesper Singer
(14147,-1,90128,90,-1,-1,0,0), -- Vesper Singer
(14139,-1,90128,91,-1,-1,0,0), -- Vesper Retributer
(14140,-1,90128,92,-1,-1,0,0), -- Vesper Retributer
(14141,-1,90128,93,-1,-1,0,0), -- Vesper Retributer

-- 
(4869,-1,90129,1,-1,-1,0,0), -- Crystal Staff
(4868,-1,90129,2,-1,-1,0,0), -- Crystal Staff
(4867,-1,90129,3,-1,-1,0,0), -- Crystal Staff
(4873,-1,90129,4,-1,-1,0,0), -- Cursed Staff
(4874,-1,90129,5,-1,-1,0,0), -- Cursed Staff
(4875,-1,90129,6,-1,-1,0,0), -- Cursed Staff
(7716,-1,90129,7,-1,-1,0,0), -- Inferno Staff
(7718,-1,90129,8,-1,-1,0,0), -- Inferno Staff
(7717,-1,90129,9,-1,-1,0,0), -- Inferno Staff
(4878,-1,90129,10,-1,-1,0,0), -- Paradia Staff
(4877,-1,90129,12,-1,-1,0,0), -- Paradia Staff
(4882,-1,90129,13,-1,-1,0,0), -- Sage's Staff
(4883,-1,90129,14,-1,-1,0,0), -- Sage's Staff
(4884,-1,90129,15,-1,-1,0,0), -- Sage's Staff
(4890,-1,90129,16,-1,-1,0,0), -- Deadman's Staff
(4888,-1,90129,17,-1,-1,0,0), -- Deadman's Staff
(4889,-1,90129,18,-1,-1,0,0), -- Deadman's Staff
(4896,-1,90129,19,-1,-1,0,0), -- Demon's Staff
(4894,-1,90129,20,-1,-1,0,0), -- Demon's Staff
(4895,-1,90129,21,-1,-1,0,0), -- Demon's Staff
(4893,-1,90129,22,-1,-1,0,0), -- Ghoul's Staff
(4892,-1,90129,23,-1,-1,0,0), -- Ghoul's Staff
(4891,-1,90129,24,-1,-1,0,0), -- Ghoul's Staff
(4899,-1,90129,25,-1,-1,0,0), -- Sprite's Staff
(4897,-1,90129,26,-1,-1,0,0), -- Sprite's Staff
(4898,-1,90129,27,-1,-1,0,0), -- Sprite's Staff
(4901,-1,90129,28,-1,-1,0,0), -- Staff of Evil Spirits
(4900,-1,90129,29,-1,-1,0,0), -- Staff of Evil Spirits
(4902,-1,90129,30,-1,-1,0,0), -- Staff of Evil Spirits
(5598,-1,90129,31,-1,-1,0,0), -- Dasparion's Staff
(5597,-1,90129,32,-1,-1,0,0), -- Dasparion's Staff
(5596,-1,90129,33,-1,-1,0,0), -- Dasparion's Staff
(4905,-1,90129,34,-1,-1,0,0), -- Dasparion's Staff
(4904,-1,90129,35,-1,-1,0,0), -- Dasparion's Staff
(4903,-1,90129,36,-1,-1,0,0), -- Dasparion's Staff
(5605,-1,90129,37,-1,-1,0,0), -- Branch of the Mother Tree
(5606,-1,90129,38,-1,-1,0,0), -- Branch of the Mother Tree
(5607,-1,90129,39,-1,-1,0,0), -- Branch of the Mother Tree
(8819,-1,90129,40,-1,-1,0,0), -- Daimon Crystal
(8818,-1,90129,41,-1,-1,0,0), -- Daimon Crystal
(8820,-1,90129,42,-1,-1,0,0), -- Daimon Crystal
(6587,-1,90129,43,-1,-1,0,0), -- Imperial Staff
(6589,-1,90129,44,-1,-1,0,0), -- Imperial Staff
(6588,-1,90129,45,-1,-1,0,0), -- Imperial Staff
(9875,-1,90129,46,-1,-1,0,0), -- Dynasty Mace
(9876,-1,90129,47,-1,-1,0,0), -- Dynasty Mace
(9877,-1,90129,48,-1,-1,0,0), -- Dynasty Mace
(14142,-1,90129,49,-1,-1,0,0), -- Vesper Caster
(14143,-1,90129,50,-1,-1,0,0), -- Vesper Caster
(14144,-1,90129,51,-1,-1,0,0), -- Vesper Caster

-- 
(23,-1,90241,1,-1,-1,0,0), -- Wooden Breastplate
(2386,-1,90241,2,-1,-1,0,0), -- Wooden Gaiters
(43,-1,90241,3,-1,-1,0,0), -- Wooden Helmet
(1101,-1,90241,7,-1,-1,0,0), -- Tunic of Devotion
(1104,-1,90241,8,-1,-1,0,0), -- Stockings of Devotion
(44,-1,90241,9,-1,-1,0,0), -- Leather Helmet
(394,-1,90241,13,-1,-1,0,0), -- Reinforced Leather Shirt
(416,-1,90241,14,-1,-1,0,0), -- Reinforced Leather Gaiters
(2446,-1,90241,16,-1,-1,0,0), -- Reinforced Leather Gloves
(2422,-1,90241,17,-1,-1,0,0), -- Reinforced Leather Boots
(395,-1,90241,19,-1,-1,0,0), -- Manticore Skin Shirt
(417,-1,90241,20,-1,-1,0,0), -- Manticore Skin Gaiters
(2424,-1,90241,23,-1,-1,0,0), -- Manticore Skin Boots
(58,-1,90241,25,-1,-1,0,0), -- Mithril Breastplate
(59,-1,90241,26,-1,-1,0,0), -- Mithril Gaiters
(47,-1,90241,27,-1,-1,0,0), -- Helmet
(61,-1,90241,28,-1,-1,0,0), -- Mithril Plate Gloves
(628,-1,90241,30,-1,-1,0,0), -- Hoplon
(436,-1,90241,31,-1,-1,0,0), -- Tunic of Knowledge
(469,-1,90241,32,-1,-1,0,0), -- Stockings of Knowledge
(2447,-1,90241,34,-1,-1,0,0), -- Gloves of Knowledge
(2423,-1,90241,35,-1,-1,0,0), -- Boots of Knowledge
(437,-1,90241,37,-1,-1,0,0), -- Mithril Tunic
(470,-1,90241,38,-1,-1,0,0), -- Mithril Stockings
(2450,-1,90241,40,-1,-1,0,0), -- Mithril Gloves
(2426,-1,90241,41,-1,-1,0,0), -- Elven Mithril Boots

-- 
(354,-1,90242,1,-1,-1,0,0), -- Chain Mail Shirt
(381,-1,90242,2,-1,-1,0,0), -- Chain Gaiters
(2413,-1,90242,3,-1,-1,0,0), -- Chain Hood
(2453,-1,90242,4,-1,-1,0,0), -- Chain Gloves
(2429,-1,90242,5,-1,-1,0,0), -- Chain Boots
(2495,-1,90242,6,-1,-1,0,0), -- Chain Shield
(60,-1,90242,7,-1,-1,0,0), -- Composite Armor
(517,-1,90242,9,-1,-1,0,0), -- Composite Helmet
(64,-1,90242,11,-1,-1,0,0), -- Composite Boots
(107,-1,90242,12,-1,-1,0,0), -- Composite Shield
(356,-1,90242,13,-1,-1,0,0), -- Full Plate Armor
(2414,-1,90242,15,-1,-1,0,0), -- Full Plate Helmet
(2462,-1,90242,16,-1,-1,0,0), -- Full Plate Gauntlets
(2438,-1,90242,17,-1,-1,0,0), -- Full Plate Boots
(2497,-1,90242,18,-1,-1,0,0), -- Full Plate Shield
(397,-1,90242,19,-1,-1,0,0), -- Mithril Shirt
(2387,-1,90242,20,-1,-1,0,0), -- Tempered Mithril Gaiters
(62,-1,90242,23,-1,-1,0,0), -- Mithril Boots
(398,-1,90242,25,-1,-1,0,0), -- Plated Leather
(418,-1,90242,26,-1,-1,0,0), -- Plated Leather Gaiters
(2431,-1,90242,29,-1,-1,0,0), -- Plated Leather Boots
(400,-1,90242,31,-1,-1,0,0), -- Theca Leather Armor
(420,-1,90242,32,-1,-1,0,0), -- Theca Leather Gaiters
(2460,-1,90242,34,-1,-1,0,0), -- Theca Leather Gloves
(2436,-1,90242,35,-1,-1,0,0), -- Theca Leather Boots
(439,-1,90242,37,-1,-1,0,0), -- Karmian Tunic
(471,-1,90242,38,-1,-1,0,0), -- Karmian Stockings
(2454,-1,90242,40,-1,-1,0,0), -- Karmian Gloves
(2430,-1,90242,41,-1,-1,0,0), -- Karmian Boots
(441,-1,90242,43,-1,-1,0,0), -- Demon's Tunic
(472,-1,90242,44,-1,-1,0,0), -- Demon's Stockings
(2459,-1,90242,46,-1,-1,0,0), -- Demon's Gloves
(2435,-1,90242,47,-1,-1,0,0), -- Demon's Boots
(442,-1,90242,49,-1,-1,0,0), -- Divine Tunic
(473,-1,90242,50,-1,-1,0,0), -- Divine Stockings
(2463,-1,90242,52,-1,-1,0,0), -- Divine Gloves
(603,-1,90242,53,-1,-1,0,0), -- Divine Boots

-- 
(357,-1,90243,1,-1,-1,0,0), -- Zubei's Breastplate
(383,-1,90243,2,-1,-1,0,0), -- Zubei's Gaiters
(503,-1,90243,3,-1,-1,0,0), -- Zubei's Helmet
(5710,-1,90243,4,-1,-1,0,0), -- Zubei's Gauntlets
(5726,-1,90243,5,-1,-1,0,0), -- Zubei's Boots
(2384,-1,90243,7,-1,-1,0,0), -- Zubei's Leather Shirt
(2388,-1,90243,8,-1,-1,0,0), -- Zubei's Leather Gaiters
(503,-1,90243,9,-1,-1,0,0), -- Zubei's Helmet
(5711,-1,90243,10,-1,-1,0,0), -- Zubei's Gauntlets
(5727,-1,90243,11,-1,-1,0,0), -- Zubei's Boots
(2397,-1,90243,13,-1,-1,0,0), -- Tunic of Zubei
(2402,-1,90243,14,-1,-1,0,0), -- Stockings of Zubei
(503,-1,90243,15,-1,-1,0,0), -- Zubei's Helmet
(5710,-1,90243,16,-1,-1,0,0), -- Zubei's Gauntlets
(5726,-1,90243,17,-1,-1,0,0), -- Zubei's Boots
(2376,-1,90243,19,-1,-1,0,0), -- Avadon Breastplate
(2379,-1,90243,20,-1,-1,0,0), -- Avadon Gaiters
(2415,-1,90243,21,-1,-1,0,0), -- Avadon Circlet
(5714,-1,90243,22,-1,-1,0,0), -- Avadon Gloves
(5730,-1,90243,23,-1,-1,0,0), -- Avadon Boots
(673,-1,90243,24,-1,-1,0,0), -- Avadon Shield
(2390,-1,90243,25,-1,-1,0,0), -- Avadon Leather Armor
(2415,-1,90243,27,-1,-1,0,0), -- Avadon Circlet
(5715,-1,90243,28,-1,-1,0,0), -- Avadon Gloves
(5731,-1,90243,29,-1,-1,0,0), -- Avadon Boots
(2406,-1,90243,31,-1,-1,0,0), -- Avadon Robe
(2415,-1,90243,33,-1,-1,0,0), -- Avadon Circlet
(5716,-1,90243,34,-1,-1,0,0), -- Avadon Gloves
(5732,-1,90243,35,-1,-1,0,0), -- Avadon Boots
(358,-1,90243,37,-1,-1,0,0), -- Blue Wolf Breastplate
(2380,-1,90243,38,-1,-1,0,0), -- Blue Wolf Gaiters
(2416,-1,90243,39,-1,-1,0,0), -- Blue Wolf Helmet
(5718,-1,90243,40,-1,-1,0,0), -- Blue Wolf Gloves
(2391,-1,90243,42,-1,-1,0,0), -- Blue Wolf Leather Armor
(2416,-1,90243,44,-1,-1,0,0), -- Blue Wolf Helmet
(5719,-1,90243,45,-1,-1,0,0), -- Blue Wolf Gloves
(5735,-1,90243,46,-1,-1,0,0), -- Blue Wolf Boots
(2398,-1,90243,48,-1,-1,0,0), -- Blue Wolf Tunic
(2403,-1,90243,49,-1,-1,0,0), -- Blue Wolf Stockings
(2416,-1,90243,50,-1,-1,0,0), -- Blue Wolf Helmet
(5718,-1,90243,51,-1,-1,0,0), -- Blue Wolf Gloves
(5734,-1,90243,52,-1,-1,0,0), -- Blue Wolf Boots
(2391,-1,90243,54,-1,-1,0,0), -- Blue Wolf Leather Armor
(2416,-1,90243,56,-1,-1,0,0), -- Blue Wolf Helmet
(5718,-1,90243,57,-1,-1,0,0), -- Blue Wolf Gloves
(5734,-1,90243,58,-1,-1,0,0), -- Blue Wolf Boots
(2398,-1,90243,60,-1,-1,0,0), -- Blue Wolf Tunic
(2403,-1,90243,61,-1,-1,0,0), -- Blue Wolf Stockings
(2416,-1,90243,62,-1,-1,0,0), -- Blue Wolf Helmet
(5719,-1,90243,63,-1,-1,0,0), -- Blue Wolf Gloves
(5735,-1,90243,64,-1,-1,0,0), -- Blue Wolf Boots
(2381,-1,90243,66,-1,-1,0,0), -- Doom Plate Armor
(2417,-1,90243,68,-1,-1,0,0), -- Doom Helmet
(5722,-1,90243,69,-1,-1,0,0), -- Doom Gloves
(5738,-1,90243,70,-1,-1,0,0), -- Doom Boots
(110,-1,90243,71,-1,-1,0,0), -- Doom Shield
(2392,-1,90243,72,-1,-1,0,0), -- Leather Armor of Doom
(2417,-1,90243,74,-1,-1,0,0), -- Doom Helmet
(5723,-1,90243,75,-1,-1,0,0), -- Doom Gloves
(5739,-1,90243,76,-1,-1,0,0), -- Doom Boots
(110,-1,90243,77,-1,-1,0,0), -- Doom Shield
(2399,-1,90243,78,-1,-1,0,0), -- Tunic of Doom
(2404,-1,90243,79,-1,-1,0,0), -- Stockings of Doom
(2417,-1,90243,80,-1,-1,0,0), -- Doom Helmet
(5724,-1,90243,81,-1,-1,0,0), -- Doom Gloves
(5740,-1,90243,82,-1,-1,0,0), -- Doom Boots
(110,-1,90243,83,-1,-1,0,0), -- Doom Shield

-- 
(365,-1,90244,1,-1,-1,0,0), -- Dark Crystal Breastplate
(388,-1,90244,2,-1,-1,0,0), -- Dark Crystal Gaiters
(512,-1,90244,3,-1,-1,0,0), -- Dark Crystal Helmet
(5765,-1,90244,4,-1,-1,0,0), -- Dark Crystal Gloves
(5777,-1,90244,5,-1,-1,0,0), -- Dark Crystal Boots
(641,-1,90244,6,-1,-1,0,0), -- Dark Crystal Shield
(2385,-1,90244,7,-1,-1,0,0), -- Dark Crystal Leather Armor
(2389,-1,90244,8,-1,-1,0,0), -- Dark Crystal Leggings
(512,-1,90244,9,-1,-1,0,0), -- Dark Crystal Helmet
(5766,-1,90244,10,-1,-1,0,0), -- Dark Crystal Gloves
(5778,-1,90244,11,-1,-1,0,0), -- Dark Crystal Boots
(641,-1,90244,12,-1,-1,0,0), -- Dark Crystal Shield
(2407,-1,90244,13,-1,-1,0,0), -- Dark Crystal Robe
(512,-1,90244,15,-1,-1,0,0), -- Dark Crystal Helmet
(5767,-1,90244,16,-1,-1,0,0), -- Dark Crystal Gloves
(5779,-1,90244,17,-1,-1,0,0), -- Dark Crystal Boots
(641,-1,90244,18,-1,-1,0,0), -- Dark Crystal Shield
(2382,-1,90244,19,-1,-1,0,0), -- Tallum Plate Armor
(547,-1,90244,21,-1,-1,0,0), -- Tallum Helm
(5768,-1,90244,22,-1,-1,0,0), -- Tallum Gloves
(5780,-1,90244,23,-1,-1,0,0), -- Tallum Boots
(2393,-1,90244,25,-1,-1,0,0), -- Tallum Leather Armor
(547,-1,90244,27,-1,-1,0,0), -- Tallum Helm
(5769,-1,90244,28,-1,-1,0,0), -- Tallum Gloves
(5781,-1,90244,29,-1,-1,0,0), -- Tallum Boots
(2400,-1,90244,31,-1,-1,0,0), -- Tallum Tunic
(2405,-1,90244,32,-1,-1,0,0), -- Tallum Stockings
(547,-1,90244,33,-1,-1,0,0), -- Tallum Helm
(5770,-1,90244,34,-1,-1,0,0), -- Tallum Gloves
(5782,-1,90244,35,-1,-1,0,0), -- Tallum Boots
(374,-1,90244,37,-1,-1,0,0), -- Armor of Nightmare
(2418,-1,90244,39,-1,-1,0,0), -- Helm of Nightmare
(5771,-1,90244,40,-1,-1,0,0), -- Gauntlets of Nightmare
(5783,-1,90244,41,-1,-1,0,0), -- Boots of Nightmare
(2498,-1,90244,42,-1,-1,0,0), -- Shield of Nightmare
(2394,-1,90244,43,-1,-1,0,0), -- Nightmarish Leather Armor
(2418,-1,90244,45,-1,-1,0,0), -- Helm of Nightmare
(5772,-1,90244,46,-1,-1,0,0), -- Gauntlets of Nightmare
(5784,-1,90244,47,-1,-1,0,0), -- Boots of Nightmare
(2498,-1,90244,48,-1,-1,0,0), -- Shield of Nightmare
(2408,-1,90244,49,-1,-1,0,0), -- Nightmare Robe
(2418,-1,90244,51,-1,-1,0,0), -- Helm of Nightmare
(5773,-1,90244,52,-1,-1,0,0), -- Gauntlets of Nightmare
(5785,-1,90244,53,-1,-1,0,0), -- Boots of Nightmare
(2498,-1,90244,54,-1,-1,0,0), -- Shield of Nightmare
(2383,-1,90244,55,-1,-1,0,0), -- Majestic Plate Armor
(2419,-1,90244,57,-1,-1,0,0), -- Majestic Circlet
(5774,-1,90244,58,-1,-1,0,0), -- Majestic Gauntlets
(5786,-1,90244,59,-1,-1,0,0), -- Majestic Boots
(2395,-1,90244,61,-1,-1,0,0), -- Majestic Leather Armor
(2419,-1,90244,63,-1,-1,0,0), -- Majestic Circlet
(5775,-1,90244,64,-1,-1,0,0), -- Majestic Gauntlets
(5787,-1,90244,65,-1,-1,0,0), -- Majestic Boots
(2409,-1,90244,67,-1,-1,0,0), -- Majestic Robe
(2419,-1,90244,69,-1,-1,0,0), -- Majestic Circlet
(5776,-1,90244,70,-1,-1,0,0), -- Majestic Gauntlets
(5788,-1,90244,71,-1,-1,0,0), -- Majestic Boots

-- 
(6373,-1,90245,1,-1,-1,0,0), -- Imperial Crusader Breastplate
(6374,-1,90245,2,-1,-1,0,0), -- Imperial Crusader Gaiters
(6378,-1,90245,3,-1,-1,0,0), -- Imperial Crusader Helmet
(6375,-1,90245,4,-1,-1,0,0), -- Imperial Crusader Gauntlets
(6376,-1,90245,5,-1,-1,0,0), -- Imperial Crusader Boots
(6377,-1,90245,6,-1,-1,0,0), -- Imperial Crusader Shield
(6379,-1,90245,7,-1,-1,0,0), -- Draconic Leather Armor
(6382,-1,90245,9,-1,-1,0,0), -- Draconic Leather Helmet
(6380,-1,90245,10,-1,-1,0,0), -- Draconic Leather Gloves
(6381,-1,90245,11,-1,-1,0,0), -- Draconic Leather Boots
(634,-1,90245,12,-1,-1,0,0), -- Dragon Shield
(6383,-1,90245,13,-1,-1,0,0), -- Major Arcana Robe
(6386,-1,90245,15,-1,-1,0,0), -- Major Arcana Circlet
(6384,-1,90245,16,-1,-1,0,0), -- Major Arcana Gloves
(6385,-1,90245,17,-1,-1,0,0), -- Major Arcana Boots

-- 
(7851,-1,90246,1,-1,-1,0,0), -- Clan Oath Armor
(7850,-1,90246,3,-1,-1,0,0), -- Clan Oath Helm
(7852,-1,90246,4,-1,-1,0,0), -- Clan Oath Gauntlets
(7853,-1,90246,5,-1,-1,0,0), -- Clan Oath Sabaton
(630,-1,90246,6,-1,-1,0,0), -- Square Shield
(7854,-1,90246,7,-1,-1,0,0), -- Clan Oath Brigandine
(7850,-1,90246,9,-1,-1,0,0), -- Clan Oath Helm
(7855,-1,90246,10,-1,-1,0,0), -- Clan Oath Leather Gloves
(7856,-1,90246,11,-1,-1,0,0), -- Clan Oath Boots
(630,-1,90246,12,-1,-1,0,0), -- Square Shield
(7857,-1,90246,13,-1,-1,0,0), -- Clan Oath Aketon
(7850,-1,90246,15,-1,-1,0,0), -- Clan Oath Helm
(7858,-1,90246,16,-1,-1,0,0), -- Clan Oath Padded Gloves
(7859,-1,90246,17,-1,-1,0,0), -- Clan Oath Sandals
(630,-1,90246,18,-1,-1,0,0), -- Square Shield
(7861,-1,90246,19,-1,-1,0,0), -- Apella Plate Armor
(7860,-1,90246,21,-1,-1,0,0), -- Apella Helm
(7862,-1,90246,22,-1,-1,0,0), -- Apella Gauntlet
(7863,-1,90246,23,-1,-1,0,0), -- Apella Solleret
(9274,-1,90246,24,-1,-1,0,0), -- Blinzlasher
(7864,-1,90246,25,-1,-1,0,0), -- Apella Brigandine
(7860,-1,90246,27,-1,-1,0,0), -- Apella Helm
(7865,-1,90246,28,-1,-1,0,0), -- Apella Leather Gloves
(7866,-1,90246,29,-1,-1,0,0), -- Apella Boots
(9274,-1,90246,30,-1,-1,0,0), -- Blinzlasher
(7867,-1,90246,31,-1,-1,0,0), -- Apella Doublet
(7860,-1,90246,33,-1,-1,0,0), -- Apella Helm
(7868,-1,90246,34,-1,-1,0,0), -- Apella Silk Gloves
(7869,-1,90246,35,-1,-1,0,0), -- Apella Sandals
(9274,-1,90246,36,-1,-1,0,0), -- Blinzlasher

-- 
(9416,-1,90247,1,-1,-1,0,0), -- Dynasty Breast Plate
(9417,-1,90247,2,-1,-1,0,0), -- Dynasty Breast Plate
(9418,-1,90247,3,-1,-1,0,0), -- Dynasty Breast Plate
(9419,-1,90247,4,-1,-1,0,0), -- Dynasty Breast Plate
(9420,-1,90247,5,-1,-1,0,0), -- Dynasty Breast Plate
(9421,-1,90247,6,-1,-1,0,0), -- Dynasty Gaiter
(9422,-1,90247,7,-1,-1,0,0), -- Dynasty Helmet
(9423,-1,90247,8,-1,-1,0,0), -- Dynasty Gauntlet
(9424,-1,90247,9,-1,-1,0,0), -- Dynasty Boots
(9441,-1,90247,10,-1,-1,0,0), -- Dynasty Shield
(9425,-1,90247,11,-1,-1,0,0), -- Dynasty Leather Armor
(9426,-1,90247,12,-1,-1,0,0), -- Dynasty Leather Armor
(9427,-1,90247,13,-1,-1,0,0), -- Dynasty Leather Armor
(10126,-1,90247,14,-1,-1,0,0), -- Dynasty Leather Armor
(10127,-1,90247,15,-1,-1,0,0), -- Dynasty Leather Armor
(10168,-1,90247,16,-1,-1,0,0), -- Dynasty Leather Armor
(10214,-1,90247,17,-1,-1,0,0), -- Dynasty Leather Armor
(9428,-1,90247,18,-1,-1,0,0), -- Dynasty Leather Leggings
(9429,-1,90247,19,-1,-1,0,0), -- Dynasty Leather Helmet
(9430,-1,90247,20,-1,-1,0,0), -- Dynasty Leather Gloves
(9431,-1,90247,21,-1,-1,0,0), -- Dynasty Leather Boots
(9432,-1,90247,22,-1,-1,0,0), -- Dynasty Tunic
(9433,-1,90247,23,-1,-1,0,0), -- Dynasty Tunic
(9434,-1,90247,24,-1,-1,0,0), -- Dynasty Tunic
(9435,-1,90247,25,-1,-1,0,0), -- Dynasty Tunic
(9436,-1,90247,26,-1,-1,0,0), -- Dynasty Tunic
(9437,-1,90247,27,-1,-1,0,0), -- Dynasty Stockings
(9438,-1,90247,28,-1,-1,0,0), -- Dynasty Circlet
(9439,-1,90247,29,-1,-1,0,0), -- Dynasty Gloves
(9440,-1,90247,30,-1,-1,0,0), -- Dynasty Shoes
(10227,-1,90247,31,-1,-1,0,0), -- Dynasty Platinum Plate
(10228,-1,90247,32,-1,-1,0,0), -- Dynasty Platinum Plate
(10229,-1,90247,33,-1,-1,0,0), -- Dynasty Platinum Plate
(10230,-1,90247,34,-1,-1,0,0), -- Dynasty Platinum Plate
(10231,-1,90247,35,-1,-1,0,0), -- Dynasty Platinum Plate
(10232,-1,90247,36,-1,-1,0,0), -- Dynasty Jewel Leather Mail
(10233,-1,90247,37,-1,-1,0,0), -- Dynasty Jewel Leather Mail
(10234,-1,90247,38,-1,-1,0,0), -- Dynasty Jewel Leather Mail
(10235,-1,90247,39,-1,-1,0,0), -- Dynasty Silver Satin Tunic
(10236,-1,90247,40,-1,-1,0,0), -- Dynasty Silver Satin Tunic
(10237,-1,90247,41,-1,-1,0,0), -- Dynasty Silver Satin Tunic
(10238,-1,90247,42,-1,-1,0,0), -- Dynasty Silver Satin Tunic
(10239,-1,90247,43,-1,-1,0,0), -- Dynasty Silver Satin Tunic
(10487,-1,90247,44,-1,-1,0,0), -- Dynasty Jeweled Leather Armor
(10488,-1,90247,45,-1,-1,0,0), -- Dynasty Jeweled Leather Armor
(10489,-1,90247,46,-1,-1,0,0), -- Dynasty Jeweled Leather Armor
(10490,-1,90247,47,-1,-1,0,0), -- Dynasty Jeweled Leather Armor
(13432,-1,90247,48,-1,-1,0,0), -- Vesper Breastplate
(13438,-1,90247,49,-1,-1,0,0), -- Vesper Gaiters
(13137,-1,90247,50,-1,-1,0,0), -- Vesper Helmet
(13439,-1,90247,51,-1,-1,0,0), -- Vesper Gauntlet
(13440,-1,90247,52,-1,-1,0,0), -- Vesper Boots
(13433,-1,90247,53,-1,-1,0,0), -- Vesper Leather Breastplate
(13441,-1,90247,54,-1,-1,0,0), -- Vesper Leather Leggings
(13138,-1,90247,55,-1,-1,0,0), -- Vesper Leather Helmet
(13442,-1,90247,56,-1,-1,0,0), -- Vesper Leather Gloves
(13443,-1,90247,57,-1,-1,0,0), -- Vesper Leather Boots
(13434,-1,90247,58,-1,-1,0,0), -- Vesper Tunic
(13444,-1,90247,59,-1,-1,0,0), -- Vesper Stockings
(13139,-1,90247,60,-1,-1,0,0), -- Vesper Circlet
(13445,-1,90247,61,-1,-1,0,0), -- Vesper Gloves
(13446,-1,90247,62,-1,-1,0,0), -- Vesper Shoes
(13471,-1,90247,63,-1,-1,0,0), -- Vesper Shield
(12813,-1,90247,64,-1,-1,0,0), -- Vesper Sigil
(13435,-1,90247,65,-1,-1,0,0), -- Vesper Noble Breastplate
(13448,-1,90247,66,-1,-1,0,0), -- Vesper Noble Gaiters
(13140,-1,90247,67,-1,-1,0,0), -- Vesper Noble Helmet
(13449,-1,90247,68,-1,-1,0,0), -- Vesper Noble Gauntlet
(13450,-1,90247,69,-1,-1,0,0), -- Vesper Noble Boots
(13436,-1,90247,70,-1,-1,0,0), -- Vesper Noble Leather Breastplate
(13451,-1,90247,71,-1,-1,0,0), -- Vesper Noble Leather Leggings
(13141,-1,90247,72,-1,-1,0,0), -- Vesper Noble Leather Helmet
(13452,-1,90247,73,-1,-1,0,0), -- Vesper Noble Leather Gloves
(13453,-1,90247,74,-1,-1,0,0), -- Vesper Noble Leather Boots
(13437,-1,90247,75,-1,-1,0,0), -- Vesper Noble Tunic
(13454,-1,90247,76,-1,-1,0,0), -- Vesper Noble Stockings
(13142,-1,90247,77,-1,-1,0,0), -- Vesper Noble Circlet
(13455,-1,90247,78,-1,-1,0,0), -- Vesper Noble Gloves
(13456,-1,90247,79,-1,-1,0,0), -- Vesper Noble Shoes

-- 
(118,-1,90311,1,-1,-1,0,0), -- Necklace of Magic
(112,-1,90311,2,-1,-1,0,0), -- Apprentice's Earring
(116,-1,90311,3,-1,-1,0,0), -- Magic Ring
(906,-1,90311,4,-1,-1,0,0), -- Necklace of Knowledge
(113,-1,90311,5,-1,-1,0,0), -- Mystic's Earring
(875,-1,90311,6,-1,-1,0,0), -- Ring of Knowledge
(1507,-1,90311,7,-1,-1,0,0), -- Necklace of Valor
(114,-1,90311,8,-1,-1,0,0), -- Earring of Strength
(1508,-1,90311,9,-1,-1,0,0), -- Ring of Raccoon
(907,-1,90311,10,-1,-1,0,0), -- Necklace of Anguish
(845,-1,90311,11,-1,-1,0,0), -- Cat's Eye Earring
(1509,-1,90311,12,-1,-1,0,0), -- Ring of Firefly
(908,-1,90311,13,-1,-1,0,0), -- Necklace of Wisdom
(877,-1,90311,14,-1,-1,0,0), -- Ring of Wisdom
(115,-1,90311,15,-1,-1,0,0), -- Earring of Wisdom
(909,-1,90311,16,-1,-1,0,0), -- Blue Diamond Necklace
(846,-1,90311,17,-1,-1,0,0), -- Coral Earring
(878,-1,90311,18,-1,-1,0,0), -- Blue Coral Ring
(910,-1,90311,19,-1,-1,0,0), -- Necklace of Devotion
(847,-1,90311,20,-1,-1,0,0), -- Red Crescent Earring
(890,-1,90311,21,-1,-1,0,0), -- Ring of Devotion
(911,-1,90311,22,-1,-1,0,0), -- Enchanted Necklace
(848,-1,90311,23,-1,-1,0,0), -- Enchanted Earring
(879,-1,90311,24,-1,-1,0,0), -- Enchanted Ring
(912,-1,90311,25,-1,-1,0,0), -- Near Forest Necklace
(849,-1,90311,26,-1,-1,0,0), -- Tiger's Eye Earring
(880,-1,90311,27,-1,-1,0,0), -- Black Pearl Ring
(913,-1,90311,28,-1,-1,0,0), -- Elven Necklace
(850,-1,90311,29,-1,-1,0,0), -- Elven Earring
(881,-1,90311,30,-1,-1,0,0), -- Elven Ring
(914,-1,90311,31,-1,-1,0,0), -- Necklace of Darkness
(851,-1,90311,32,-1,-1,0,0), -- OMENBeast's Eye Earring
(882,-1,90311,33,-1,-1,0,0), -- Mithril Ring

-- 
(915,-1,90312,1,-1,-1,0,0), -- Aquastone Necklace
(852,-1,90312,2,-1,-1,0,0), -- Moonstone Earring
(883,-1,90312,3,-1,-1,0,0), -- Aquastone Ring
(916,-1,90312,4,-1,-1,0,0), -- Necklace of Protection
(853,-1,90312,5,-1,-1,0,0), -- Earring of Protection
(884,-1,90312,6,-1,-1,0,0), -- Ring of Protection
(917,-1,90312,7,-1,-1,0,0), -- Necklace of Mermaid
(854,-1,90312,8,-1,-1,0,0), -- Earring of Binding
(885,-1,90312,9,-1,-1,0,0), -- Ring of Ages
(119,-1,90312,10,-1,-1,0,0), -- Necklace of Binding
(857,-1,90312,11,-1,-1,0,0), -- Blessed Earring
(886,-1,90312,12,-1,-1,0,0), -- Ring of Binding
(919,-1,90312,13,-1,-1,0,0), -- Blessed Necklace
(855,-1,90312,14,-1,-1,0,0), -- Nassen's Earring
(888,-1,90312,15,-1,-1,0,0), -- Blessed Ring

-- 
(927,-1,90313,1,-1,-1,0,0), -- Necklace of Summoning
(865,-1,90313,2,-1,-1,0,0), -- Earring of Summoning
(896,-1,90313,3,-1,-1,0,0), -- Ring of Summoning
(925,-1,90313,4,-1,-1,0,0), -- Necklace of Solar Eclipse
(863,-1,90313,5,-1,-1,0,0), -- Earring of Solar Eclipse
(894,-1,90313,6,-1,-1,0,0), -- Ring of Solar Eclipse
(936,-1,90313,7,-1,-1,0,0), -- Necklace of Blessing
(874,-1,90313,8,-1,-1,0,0), -- Earring of Blessing
(905,-1,90313,9,-1,-1,0,0), -- Ring of Blessing
(918,-1,90313,10,-1,-1,0,0), -- Adamantite Necklace
(856,-1,90313,11,-1,-1,0,0), -- Adamantite Earring
(887,-1,90313,12,-1,-1,0,0), -- Adamantite Ring
(931,-1,90313,13,-1,-1,0,0), -- Necklace of Grace
(869,-1,90313,14,-1,-1,0,0), -- Earring of Grace
(900,-1,90313,15,-1,-1,0,0), -- Ring of Grace
(873,-1,90313,16,-1,-1,0,0), -- Earring of Aid
(935,-1,90313,17,-1,-1,0,0), -- Necklace of Aid
(904,-1,90313,18,-1,-1,0,0), -- Ring of Aid
(921,-1,90313,19,-1,-1,0,0), -- Necklace of Mana
(859,-1,90313,20,-1,-1,0,0), -- Earring of Mana
(117,-1,90313,21,-1,-1,0,0), -- Ring of Mana
(928,-1,90313,22,-1,-1,0,0), -- Otherworldly Necklace
(866,-1,90313,23,-1,-1,0,0), -- Otherworldly Earring
(897,-1,90313,24,-1,-1,0,0), -- Otherworldly Ring
(929,-1,90313,25,-1,-1,0,0), -- Elemental Necklace
(867,-1,90313,26,-1,-1,0,0), -- Elemental Earring
(898,-1,90313,27,-1,-1,0,0), -- Elemental Ring
(922,-1,90313,28,-1,-1,0,0), -- Sage's Necklace
(860,-1,90313,29,-1,-1,0,0), -- Sage's Earring
(891,-1,90313,30,-1,-1,0,0), -- Sage's Ring
(932,-1,90313,31,-1,-1,0,0), -- Necklace of Holy Spirit
(870,-1,90313,32,-1,-1,0,0), -- Earring of Holy Spirit
(901,-1,90313,33,-1,-1,0,0), -- Ring of Holy Spirit
(923,-1,90313,34,-1,-1,0,0), -- Paradia Necklace
(861,-1,90313,35,-1,-1,0,0), -- Paradia Earring
(892,-1,90313,36,-1,-1,0,0), -- Paradia Ring
(926,-1,90313,37,-1,-1,0,0), -- Necklace of Black Ore
(864,-1,90313,38,-1,-1,0,0), -- Earring of Black Ore
(895,-1,90313,39,-1,-1,0,0), -- Ring of Black Ore

-- 
(6323,-1,90314,1,-1,-1,0,0), -- Sealed Phoenix Necklace
(6324,-1,90314,2,-1,-1,0,0), -- Sealed Phoenix Earring
(6325,-1,90314,3,-1,-1,0,0), -- Sealed Phoenix Ring
(933,-1,90314,4,-1,-1,0,0), -- Phoenix Necklace
(871,-1,90314,5,-1,-1,0,0), -- Phoenix Earring
(902,-1,90314,6,-1,-1,0,0), -- Phoenix Ring
(6326,-1,90314,7,-1,-1,0,0), -- Sealed Majestic Necklace
(6327,-1,90314,8,-1,-1,0,0), -- Sealed Majestic Earring
(6328,-1,90314,9,-1,-1,0,0), -- Sealed Majestic Ring
(934,-1,90314,10,-1,-1,0,0), -- Cerberus Necklace
(872,-1,90314,11,-1,-1,0,0), -- Cerberus Earring
(903,-1,90314,12,-1,-1,0,0), -- Cerberus Ring
(930,-1,90314,13,-1,-1,0,0), -- Necklace of Phantom
(868,-1,90314,14,-1,-1,0,0), -- Earring of Phantom
(899,-1,90314,15,-1,-1,0,0), -- Ring of Phantom
(924,-1,90314,16,-1,-1,0,0), -- Majestic Necklace
(862,-1,90314,17,-1,-1,0,0), -- Majestic Earring
(893,-1,90314,18,-1,-1,0,0), -- Majestic Ring

-- 
(6726,-1,90315,1,-1,-1,0,0), -- Sealed Tateossian Necklace
(6724,-1,90315,2,-1,-1,0,0), -- Sealed Tateossian Earring
(6725,-1,90315,3,-1,-1,0,0), -- Sealed Tateossian Ring
(920,-1,90315,4,-1,-1,0,0), -- Tateossian Necklace
(858,-1,90315,5,-1,-1,0,0), -- Tateossian Earring
(889,-1,90315,6,-1,-1,0,0), -- Tateossian Ring

-- 
(9457,-1,90316,7,-1,-1,0,0), -- Dynasty Ring
(9455,-1,90316,8,-1,-1,0,0), -- Dynasty Earrings
(9456,-1,90316,9,-1,-1,0,0), -- Dynasty Necklace
(14165,-1,90316,10,-1,-1,0,0), -- Vesper Ring
(14163,-1,90316,11,-1,-1,0,0), -- Vesper Earring
(14164,-1,90316,12,-1,-1,0,0), -- Vesper Necklace

-- 
(6029,1000000000,90501,1,-1,-1,0,0), -- Lunargent
(6033,500000000,90501,2,-1,-1,0,0), -- Hellfire Oil
(1419,150000000,90501,3,-1,-1,0,0), -- Blood Mark
(3874,200000000,90501,4,-1,-1,0,0), -- Alliance Manifesto
(3870,300000000,90501,5,-1,-1,0,0), -- Seal of Aspiration
(1870,-1,90501,6,-1,-1,0,0), -- Coal
(1871,-1,90501,7,-1,-1,0,0), -- Charcoal
(1873,-1,90501,8,-1,-1,0,0), -- Silver Nugget
(8873,-1,90501,9,-1,-1,0,0), -- Phoenix Blood
(1866,-1,90501,10,-1,-1,0,0), -- Suede
(1868,-1,90501,11,-1,-1,0,0), -- Thread
(1893,-1,90501,12,-1,-1,0,0), -- Oriharukon
(1874,-1,90501,13,-1,-1,0,0), -- Oriharukon Ore
(4044,-1,90501,14,-1,-1,0,0), -- Thons
(1891,-1,90501,15,-1,-1,0,0), -- Artisan's Frame
(1882,-1,90501,16,-1,-1,0,0), -- Leather

-- 
(7076,-1,90504,1,-1,-1,0,0), -- Mysterious Cloth
(7077,-1,90504,2,-1,-1,0,0), -- Jewel Box
(7078,-1,90504,3,-1,-1,0,0), -- Sewing Kit
(7113,-1,90504,4,-1,-1,0,0), -- Dress Shoe Box
(7164,-1,90504,5,-1,-1,0,0), -- Signet Ring
(7160,-1,90504,6,-1,-1,0,0), -- Luxury Wine
(7159,-1,90504,7,-1,-1,0,0), -- Box of Cookies

-- 
(1835,-1,90511,1,-1,-1,0,0), -- Soulshot: No Grade
(1463,-1,90511,2,-1,-1,0,0), -- Soulshot: D-grade
(1464,-1,90511,3,-1,-1,0,0), -- Soulshot: C-grade
(1465,-1,90511,4,-1,-1,0,0), -- Soulshot: B-grade
(1466,-1,90511,5,-1,-1,0,0), -- Soulshot: A-grade
(1467,-1,90511,6,-1,-1,0,0), -- Soulshot: S-grade
(2509,-1,90511,7,-1,-1,0,0), -- Spiritshot: No Grade
(2510,-1,90511,8,-1,-1,0,0), -- Spiritshot: D-grade
(2511,-1,90511,9,-1,-1,0,0), -- Spiritshot: C-grade
(2512,-1,90511,10,-1,-1,0,0), -- Spiritshot: B-grade
(2513,-1,90511,11,-1,-1,0,0), -- Spiritshot: A-grade
(2514,-1,90511,12,-1,-1,0,0), -- Spiritshot: S-grade
(3947,-1,90511,13,-1,-1,0,0), -- Blessed Spiritshot: No Grade
(3948,-1,90511,14,-1,-1,0,0), -- Blessed Spiritshot: D-Grade
(3949,-1,90511,15,-1,-1,0,0), -- Blessed Spiritshot: C-Grade
(3950,-1,90511,16,-1,-1,0,0), -- Blessed Spiritshot: B-Grade
(3951,-1,90511,17,-1,-1,0,0), -- Blessed Spiritshot: A-Grade
(3952,-1,90511,18,-1,-1,0,0), -- Blessed Spiritshot: S Grade

-- 
(6645,-1,90512,1,-1,-1,0,0), -- Beast Soulshot
(6646,-1,90512,2,-1,-1,0,0), -- Beast Spiritshot
(6647,-1,90512,3,-1,-1,0,0), -- Blessed Beast Spiritshot

-- 
(6535,-1,90513,1,-1,-1,0,0), -- Fishing Shot: non-grade
(6536,-1,90513,2,-1,-1,0,0), -- Fishing Shot: D-grade
(6537,-1,90513,3,-1,-1,0,0), -- Fishing Shot: C-grade
(6538,-1,90513,4,-1,-1,0,0), -- Fishing Shot: B-grade
(6539,-1,90513,5,-1,-1,0,0), -- Fishing Shot: A-grade
(6540,-1,90513,6,-1,-1,0,0), -- Fishing Shot: S-grade

-- 
(5134,-1,90514,1,-1,-1,0,0), -- Compressed Package of Soulshots: No Grade
(5135,-1,90514,2,-1,-1,0,0), -- Compressed Package of Soulshots: D-grade
(5136,-1,90514,3,-1,-1,0,0), -- Compressed Package of Soulshots: C-grade
(5137,-1,90514,4,-1,-1,0,0), -- Compressed Package of Soulshots: B-grade
(5138,-1,90514,5,-1,-1,0,0), -- Compressed Package of Soulshots: A-grade
(5139,-1,90514,6,-1,-1,0,0), -- Compressed Package of Soulshots: S-grade
(5140,-1,90514,7,-1,-1,0,0), -- Compressed Package of Spiritshots: No Grade
(5141,-1,90514,8,-1,-1,0,0), -- Compressed Package of Spiritshots: D-grade
(5142,-1,90514,9,-1,-1,0,0), -- Compressed Package of Spiritshots: C-grade
(5143,-1,90514,10,-1,-1,0,0), -- Compressed Package of Spiritshots: B-grade
(5144,-1,90514,11,-1,-1,0,0), -- Compressed Package of Spiritshots: A-grade
(5145,-1,90514,12,-1,-1,0,0), -- Compressed Package of Spiritshots: S-grade
(5146,-1,90514,13,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: No Grade
(5147,-1,90514,14,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: D-grade
(5148,-1,90514,15,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: C-grade
(5149,-1,90514,16,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: B-grade
(5150,-1,90514,17,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: A-grade
(5151,-1,90514,18,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: S-grade
(5250,-1,90514,19,-1,-1,0,0), -- Greater Compressed Package of Soulshots: No-grade
(5251,-1,90514,20,-1,-1,0,0), -- Greater Compressed Package of Soulshots: D-grade
(5252,-1,90514,21,-1,-1,0,0), -- Greater Compressed Package of Soulshots: C-grade
(5253,-1,90514,22,-1,-1,0,0), -- Greater Compressed Package of Soulshots: B-grade
(5254,-1,90514,23,-1,-1,0,0), -- Greater Compressed Package of Soulshots: A-grade
(5255,-1,90514,24,-1,-1,0,0), -- Greater Compressed Package of Soulshots: S-grade
(5256,-1,90514,25,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: No-grade
(5257,-1,90514,26,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: D-grade
(5258,-1,90514,27,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: C-grade
(5259,-1,90514,28,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: B-grade
(5260,-1,90514,29,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: A-grade
(5261,-1,90514,30,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: S-grade
(5262,-1,90514,31,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: No-grade
(5263,-1,90514,32,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: D-grade
(5264,-1,90514,33,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: C-grade
(5265,-1,90514,34,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: B-grade
(5266,-1,90514,35,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: A-grade
(5267,-1,90514,36,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: S-grade

-- 
(1835,-1,90515,1,-1,-1,0,0), -- Soulshot: No Grade
(1463,-1,90515,2,-1,-1,0,0), -- Soulshot: D-grade
(1464,-1,90515,3,-1,-1,0,0), -- Soulshot: C-grade
(1465,-1,90515,4,-1,-1,0,0), -- Soulshot: B-grade
(1466,-1,90515,5,-1,-1,0,0), -- Soulshot: A-grade
(1467,-1,90515,6,-1,-1,0,0), -- Soulshot: S-grade
(2509,-1,90515,7,-1,-1,0,0), -- Spiritshot: No Grade
(2510,-1,90515,8,-1,-1,0,0), -- Spiritshot: D-grade
(2511,-1,90515,9,-1,-1,0,0), -- Spiritshot: C-grade
(2512,-1,90515,10,-1,-1,0,0), -- Spiritshot: B-grade
(2513,-1,90515,11,-1,-1,0,0), -- Spiritshot: A-grade
(2514,-1,90515,12,-1,-1,0,0), -- Spiritshot: S-grade
(3947,-1,90515,13,-1,-1,0,0), -- Blessed Spiritshot: No Grade
(3948,-1,90515,14,-1,-1,0,0), -- Blessed Spiritshot: D-Grade
(3949,-1,90515,15,-1,-1,0,0), -- Blessed Spiritshot: C-Grade
(3950,-1,90515,16,-1,-1,0,0), -- Blessed Spiritshot: B-Grade
(3951,-1,90515,17,-1,-1,0,0), -- Blessed Spiritshot: A-Grade
(3952,-1,90515,18,-1,-1,0,0), -- Blessed Spiritshot: S Grade
(6645,-1,90515,19,-1,-1,0,0), -- Beast Soulshot
(6646,-1,90515,20,-1,-1,0,0), -- Beast Spiritshot
(6647,-1,90515,21,-1,-1,0,0), -- Blessed Beast Spiritshot
(6535,-1,90515,22,-1,-1,0,0), -- Fishing Shot: non-grade
(6536,-1,90515,23,-1,-1,0,0), -- Fishing Shot: D-grade
(6537,-1,90515,24,-1,-1,0,0), -- Fishing Shot: C-grade
(6538,-1,90515,25,-1,-1,0,0), -- Fishing Shot: B-grade
(6539,-1,90515,26,-1,-1,0,0), -- Fishing Shot: A-grade
(6540,-1,90515,27,-1,-1,0,0), -- Fishing Shot: S-grade
(5134,-1,90515,28,-1,-1,0,0), -- Compressed Package of Soulshots: No Grade
(5135,-1,90515,29,-1,-1,0,0), -- Compressed Package of Soulshots: D-grade
(5136,-1,90515,30,-1,-1,0,0), -- Compressed Package of Soulshots: C-grade
(5137,-1,90515,31,-1,-1,0,0), -- Compressed Package of Soulshots: B-grade
(5138,-1,90515,32,-1,-1,0,0), -- Compressed Package of Soulshots: A-grade
(5139,-1,90515,33,-1,-1,0,0), -- Compressed Package of Soulshots: S-grade
(5140,-1,90515,34,-1,-1,0,0), -- Compressed Package of Spiritshots: No Grade
(5141,-1,90515,35,-1,-1,0,0), -- Compressed Package of Spiritshots: D-grade
(5142,-1,90515,36,-1,-1,0,0), -- Compressed Package of Spiritshots: C-grade
(5143,-1,90515,37,-1,-1,0,0), -- Compressed Package of Spiritshots: B-grade
(5144,-1,90515,38,-1,-1,0,0), -- Compressed Package of Spiritshots: A-grade
(5145,-1,90515,39,-1,-1,0,0), -- Compressed Package of Spiritshots: S-grade
(5146,-1,90515,40,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: No Grade
(5147,-1,90515,41,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: D-grade
(5148,-1,90515,42,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: C-grade
(5149,-1,90515,43,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: B-grade
(5150,-1,90515,44,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: A-grade
(5151,-1,90515,45,-1,-1,0,0), -- Compressed Package of Blessed Spiritshots: S-grade
(5250,-1,90515,46,-1,-1,0,0), -- Greater Compressed Package of Soulshots: No-grade
(5251,-1,90515,47,-1,-1,0,0), -- Greater Compressed Package of Soulshots: D-grade
(5252,-1,90515,48,-1,-1,0,0), -- Greater Compressed Package of Soulshots: C-grade
(5253,-1,90515,49,-1,-1,0,0), -- Greater Compressed Package of Soulshots: B-grade
(5254,-1,90515,50,-1,-1,0,0), -- Greater Compressed Package of Soulshots: A-grade
(5255,-1,90515,51,-1,-1,0,0), -- Greater Compressed Package of Soulshots: S-grade
(5256,-1,90515,52,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: No-grade
(5257,-1,90515,53,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: D-grade
(5258,-1,90515,54,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: C-grade
(5259,-1,90515,55,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: B-grade
(5260,-1,90515,56,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: A-grade
(5261,-1,90515,57,-1,-1,0,0), -- Greater Compressed Package of Spiritshots: S-grade
(5262,-1,90515,58,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: No-grade
(5263,-1,90515,59,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: D-grade
(5264,-1,90515,60,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: C-grade
(5265,-1,90515,61,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: B-grade
(5266,-1,90515,62,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: A-grade
(5267,-1,90515,63,-1,-1,0,0), -- Greater Compressed Package of Blessed Spiritshots: S-grade

-- 
(65,-1,90521,1,-1,-1,0,0), -- Red Potion
(1060,-1,90521,2,-1,-1,0,0), -- Lesser Healing Potion
(733,-1,90521,3,-1,-1,0,0), -- Endeavor Potion
(1831,-1,90521,4,-1,-1,0,0), -- Antidote
(1832,-1,90521,5,-1,-1,0,0), -- Greater Antidote
(5283,-1,90521,6,-1,-1,0,0), -- Rice Cake
(726,-1,90521,7,-1,-1,0,0), -- Mana Drug
(735,-1,90521,8,-1,-1,0,0), -- Potion of Alacrity
(3889,-1,90521,9,-1,-1,0,0), -- Potion of Recovery
(4680,-1,90521,10,-1,-1,0,0), -- Potion of Revenge
(5589,-1,90521,11,-1,-1,0,0), -- Energy Stone
(1833,-1,90521,12,-1,-1,0,0), -- Bandage
(1834,-1,90521,13,-1,-1,0,0), -- Emergency Dressing

-- 
(6035,-1,90522,1,-1,-1,0,0), -- Magic Haste Potion
(1061,-1,90522,2,-1,-1,0,0), -- Healing Potion
(734,-1,90522,3,-1,-1,0,0), -- Haste Potion
(1073,-1,90522,5,-1,-1,0,0), -- Beginner's Potion
(727,-1,90522,6,-1,-1,0,0), -- Healing Potion

-- 
(1539,-1,90523,1,-1,-1,0,0), -- Greater Healing Potion
(1375,-1,90523,2,-1,-1,0,0), -- Greater Swift Attack Potion
(1374,-1,90523,3,-1,-1,0,0), -- Greater Haste Potion
(6036,-1,90523,4,-1,-1,0,0), -- Greater Magic Haste Potion
(728,-1,90523,5,-1,-1,0,0), -- Mana Potion

-- 
(5241,-1,90524,1,-1,-1,0,0), -- Dye Potion - D
(5240,-1,90524,2,-1,-1,0,0), -- Dye Potion - C
(5239,-1,90524,3,-1,-1,0,0), -- Dye Potion - B
(5238,-1,90524,4,-1,-1,0,0), -- Dye Potion - A
(5242,-1,90524,5,-1,-1,0,0), -- Hair Style Change Potion - A
(5243,-1,90524,6,-1,-1,0,0), -- Hair Style Change Potion - B
(5244,-1,90524,7,-1,-1,0,0), -- Hair Style Change Potion - C
(5245,-1,90524,8,-1,-1,0,0), -- Hair Style Change Potion - D
(5246,-1,90524,9,-1,-1,0,0), -- Hair Style Change Potion - E
(5247,-1,90524,10,-1,-1,0,0), -- Hair Style Change Potion - F
(5248,-1,90524,11,-1,-1,0,0), -- Hair Style Change Potion - G
(5235,-1,90524,12,-1,-1,0,0), -- Facelifting Potion - A
(5236,-1,90524,13,-1,-1,0,0), -- Facelifting Potion - B
(5237,-1,90524,14,-1,-1,0,0), -- Facelifting Potion - C
(5234,-1,90524,15,-1,-1,0,0), -- Mystery Potion

-- 
(736,-1,90531,1,-1,-1,0,0), -- Scroll of Escape
(737,-1,90531,2,-1,-1,0,0), -- Scroll of Resurrection
(1829,-1,90531,3,-1,-1,0,0), -- Scroll of Escape: Clan Hall
(1830,-1,90531,4,-1,-1,0,0), -- Scroll of Escape: Castle
(6037,-1,90531,5,-1,-1,0,0), -- Waking Scroll
(5965,-1,90531,6,-1,-1,0,0), -- Blank Scroll

-- 
(8594,-1,90532,1,-1,-1,0,0), -- Scroll: Recovery (No Grade)
(8595,-1,90532,2,-1,-1,0,0), -- Scroll: Recovery (Grade D)
(8596,-1,90532,3,-1,-1,0,0), -- Scroll: Recovery (Grade C)
(8597,-1,90532,4,-1,-1,0,0), -- Scroll: Recovery (Grade B)
(8598,-1,90532,5,-1,-1,0,0), -- Scroll: Recovery (Grade A)
(8599,-1,90532,6,-1,-1,0,0), -- Scroll: Recovery (Grade S)

-- 
(3931,-1,90533,1,-1,-1,0,0), -- L2Day - Scroll of Agility
(3927,-1,90533,2,-1,-1,0,0), -- L2Day - Scroll of Death Whisper
(3928,-1,90533,3,-1,-1,0,0), -- L2Day - Scroll of Focus
(3929,-1,90533,4,-1,-1,0,0), -- L2Day - Scroll of Greater Acumen
(3926,-1,90533,5,-1,-1,0,0), -- L2Day - Scroll of Guidance
(3930,-1,90533,6,-1,-1,0,0), -- L2Day - Scroll of Haste
(4218,-1,90533,7,-1,-1,0,0), -- L2 Day - Scroll of Mana Regeneration
(3933,-1,90533,8,-1,-1,0,0), -- L2Day - Scroll of Might
(3932,-1,90533,9,-1,-1,0,0), -- L2Day - Scroll of Mystic Empower
(3935,-1,90533,10,-1,-1,0,0), -- L2Day - Scroll of Shield
(3934,-1,90533,11,-1,-1,0,0), -- L2Day - Scroll of Windwalk

-- 
(956,-1,90534,1,-1,-1,0,0), -- Scroll: Enchant Armor (D)
(952,-1,90534,2,-1,-1,0,0), -- Scroll: Enchant Armor (C)
(948,-1,90534,3,-1,-1,0,0), -- Scroll: Enchant Armor (B)
(730,-1,90534,4,-1,-1,0,0), -- Scroll: Enchant Armor (A)
(960,-1,90534,5,-1,-1,0,0), -- Scroll: Enchant Armor (S)
(955,-1,90534,7,-1,-1,0,0), -- Scroll: Enchant Weapon (D)
(951,-1,90534,8,-1,-1,0,0), -- Scroll: Enchant Weapon (C)
(947,-1,90534,9,-1,-1,0,0), -- Scroll: Enchant Weapon (B)
(729,-1,90534,10,-1,-1,0,0), -- Scroll: Enchant Weapon (A)
(959,-1,90534,11,-1,-1,0,0), -- Scroll: Enchant Weapon (S)

-- 
(958,-1,90535,1,-1,-1,0,0), -- Crystal Scroll: Enchant Armor (D)
(954,-1,90535,2,-1,-1,0,0), -- Crystal Scroll: Enchant Armor (C)
(950,-1,90535,3,-1,-1,0,0), -- Crystal Scroll: Enchant Armor (B)
(732,-1,90535,4,-1,-1,0,0), -- Crystal Scroll: Enchant Armor (A)
(962,-1,90535,5,-1,-1,0,0), -- Crystal Scroll: Enchant Armor (S)
(4662,-1,90535,6,-1,-1,0,0), -- Broken Red Soul Crystal
(957,-1,90535,7,-1,-1,0,0), -- Crystal Scroll: Enchant Weapon (D)
(953,-1,90535,8,-1,-1,0,0), -- Crystal Scroll: Enchant Weapon (C)
(949,-1,90535,9,-1,-1,0,0), -- Crystal Scroll: Enchant Weapon (B)
(731,-1,90535,10,-1,-1,0,0), -- Crystal Scroll: Enchant Weapon (A)
(961,-1,90535,11,-1,-1,0,0), -- Crystal Scroll: Enchant Weapon (S)
(4663,-1,90535,12,-1,-1,0,0), -- Broken Green Soul Crystal

-- 
(6576,-1,90536,1,-1,-1,0,0), -- Blessed Scroll: Enchant Armor (D)
(6574,-1,90536,2,-1,-1,0,0), -- Blessed Scroll: Enchant Armor (C)
(6572,-1,90536,3,-1,-1,0,0), -- Blessed Scroll: Enchant Armor (B)
(6570,-1,90536,4,-1,-1,0,0), -- Blessed Scroll: Enchant Armor (A)
(6578,-1,90536,5,-1,-1,0,0), -- Blessed Scroll: Enchant Armor (S)
(4662,-1,90536,6,-1,-1,0,0), -- Broken Red Soul Crystal
(6575,-1,90536,7,-1,-1,0,0), -- Blessed Scroll: Enchant Weapon (D)
(6573,-1,90536,8,-1,-1,0,0), -- Blessed Scroll: Enchant Weapon (C)
(6571,-1,90536,9,-1,-1,0,0), -- Blessed Scroll: Enchant Weapon (B)
(6569,-1,90536,10,-1,-1,0,0), -- Blessed Scroll: Enchant Weapon (A)
(6577,-1,90536,11,-1,-1,0,0), -- Blessed Scroll: Enchant Weapon (S)
(4663,-1,90536,12,-1,-1,0,0), -- Broken Green Soul Crystal

-- Consumables
(1458,-1,90544,1,-1,-1,0,0), -- Crystal: D-Grade
(1459,-1,90544,2,-1,-1,0,0), -- Crystal: C-Grade
(1460,-1,90544,3,-1,-1,0,0), -- Crystal: B-Grade
(1461,-1,90544,4,-1,-1,0,0), -- Crystal: A-Grade
(1462,-1,90544,5,-1,-1,0,0), -- Crystal: S Grade
(1785,-1,90544,6,-1,-1,0,0), -- Soul Ore
(3031,-1,90544,7,-1,-1,0,0), -- Spirit Ore
(2508,-1,90544,8,-1,-1,0,0), -- Cursed Bone
(8874,-1,90544,9,-1,-1,0,0), -- Einhasad's Holy Water
(8876,-1,90544,10,-1,-1,0,0), -- Magic Symbol
(8875,-1,90544,11,-1,-1,0,0), -- Battle Symbol
(8519,-1,90544,12,-1,-1,0,0), -- Charm of Courage: A-Grade
(8520,-1,90544,13,-1,-1,0,0), -- Charm of Courage: S-Grade
(5589,-1,90544,14,-1,-1,0,0), -- Energy Stone
(8615,-1,90544,15,-1,-1,0,0), -- Summoning Crystal
(8873,-1,90544,16,-1,-1,0,0), -- Phoenix Blood
(2130,-1,90544,17,-1,-1,0,0), -- Gemstone D
(2131,-1,90544,18,-1,-1,0,0), -- Gemstone C
(2132,-1,90544,19,-1,-1,0,0), -- Gemstone B
(2133,-1,90544,20,-1,-1,0,0), -- Gemstone A
(2134,-1,90544,21,-1,-1,0,0), -- Gemstone S

-- 
(4445,-1,90581,1,-1,-1,0,0), -- Dye of STR
(4446,-1,90581,2,-1,-1,0,0), -- Dye of STR
(4447,-1,90581,3,-1,-1,0,0), -- Dye of CON
(4448,-1,90581,4,-1,-1,0,0), -- Dye of CON
(4449,-1,90581,5,-1,-1,0,0), -- Dye of DEX
(4450,-1,90581,6,-1,-1,0,0), -- Dye of DEX
(4451,-1,90581,7,-1,-1,0,0), -- Dye of INT
(4452,-1,90581,8,-1,-1,0,0), -- Dye of INT
(4453,-1,90581,9,-1,-1,0,0), -- Dye of MEN
(4454,-1,90581,10,-1,-1,0,0), -- Dye of MEN
(4455,-1,90581,11,-1,-1,0,0), -- Dye of WIT
(4456,-1,90581,12,-1,-1,0,0), -- Dye of WIT
(4457,-1,90581,13,-1,-1,0,0), -- Dye of STR
(4458,-1,90581,14,-1,-1,0,0), -- Dye of STR
(4459,-1,90581,15,-1,-1,0,0), -- Dye of CON
(4460,-1,90581,16,-1,-1,0,0), -- Dye of CON
(4461,-1,90581,17,-1,-1,0,0), -- Dye of DEX
(4462,-1,90581,18,-1,-1,0,0), -- Dye of DEX
(4463,-1,90581,19,-1,-1,0,0), -- Dye of INT
(4464,-1,90581,20,-1,-1,0,0), -- Dye of INT
(4465,-1,90581,21,-1,-1,0,0), -- Dye of MEN
(4466,-1,90581,22,-1,-1,0,0), -- Dye of MEN
(4467,-1,90581,23,-1,-1,0,0), -- Dye of WIT
(4468,-1,90581,24,-1,-1,0,0), -- Dye of WIT
(4469,-1,90581,25,-1,-1,0,0), -- Dye of STR
(4470,-1,90581,26,-1,-1,0,0), -- Dye of STR
(4471,-1,90581,27,-1,-1,0,0), -- Dye of CON
(4472,-1,90581,28,-1,-1,0,0), -- Dye of CON
(4473,-1,90581,29,-1,-1,0,0), -- Dye of DEX
(4474,-1,90581,30,-1,-1,0,0), -- Dye of DEX
(4475,-1,90581,31,-1,-1,0,0), -- Dye of INT
(4476,-1,90581,32,-1,-1,0,0), -- Dye of INT
(4477,-1,90581,33,-1,-1,0,0), -- Dye of MEN
(4478,-1,90581,34,-1,-1,0,0), -- Dye of MEN
(4479,-1,90581,35,-1,-1,0,0), -- Dye of WIT
(4480,-1,90581,36,-1,-1,0,0), -- Dye of WIT

-- 
(4481,-1,90582,1,-1,-1,0,0), -- Greater Dye of STR
(4482,-1,90582,2,-1,-1,0,0), -- Greater Dye of STR
(4483,-1,90582,3,-1,-1,0,0), -- Greater Dye of CON
(4484,-1,90582,4,-1,-1,0,0), -- Greater Dye of CON
(4485,-1,90582,5,-1,-1,0,0), -- Greater Dye of DEX
(4486,-1,90582,6,-1,-1,0,0), -- Greater Dye of DEX
(4487,-1,90582,7,-1,-1,0,0), -- Greater Dye of INT
(4488,-1,90582,8,-1,-1,0,0), -- Greater Dye of INT
(4489,-1,90582,9,-1,-1,0,0), -- Greater Dye of MEN
(4490,-1,90582,10,-1,-1,0,0), -- Greater Dye of MEN
(4491,-1,90582,11,-1,-1,0,0), -- Greater Dye of WIT
(4492,-1,90582,12,-1,-1,0,0), -- Greater Dye of WIT
(4493,-1,90582,13,-1,-1,0,0), -- Greater Dye of STR
(4494,-1,90582,14,-1,-1,0,0), -- Greater Dye of STR
(4495,-1,90582,15,-1,-1,0,0), -- Greater Dye of CON
(4496,-1,90582,16,-1,-1,0,0), -- Greater Dye of CON
(4497,-1,90582,17,-1,-1,0,0), -- Greater Dye of DEX
(4498,-1,90582,18,-1,-1,0,0), -- Greater Dye of DEX
(4499,-1,90582,19,-1,-1,0,0), -- Greater Dye of INT
(4500,-1,90582,20,-1,-1,0,0), -- Greater Dye of INT
(4501,-1,90582,21,-1,-1,0,0), -- Greater Dye of MEN
(4502,-1,90582,22,-1,-1,0,0), -- Greater Dye of MEN
(4503,-1,90582,23,-1,-1,0,0), -- Greater Dye of WIT
(4504,-1,90582,24,-1,-1,0,0), -- Greater Dye of WIT
(4505,-1,90582,25,-1,-1,0,0), -- Greater Dye of STR
(4506,-1,90582,26,-1,-1,0,0), -- Greater Dye of STR
(4507,-1,90582,27,-1,-1,0,0), -- Greater Dye of CON
(4508,-1,90582,28,-1,-1,0,0), -- Greater Dye of CON
(4509,-1,90582,29,-1,-1,0,0), -- Greater Dye of DEX
(4510,-1,90582,30,-1,-1,0,0), -- Greater Dye of DEX
(4511,-1,90582,31,-1,-1,0,0), -- Greater Dye of INT
(4512,-1,90582,32,-1,-1,0,0), -- Greater Dye of INT
(4513,-1,90582,33,-1,-1,0,0), -- Greater Dye of MEN
(4514,-1,90582,34,-1,-1,0,0), -- Greater Dye of MEN
(4515,-1,90582,35,-1,-1,0,0), -- Greater Dye of WIT
(4516,-1,90582,36,-1,-1,0,0), -- Greater Dye of WIT
(4517,-1,90582,37,-1,-1,0,0), -- Greater Dye of STR
(4518,-1,90582,38,-1,-1,0,0), -- Greater Dye of STR
(4519,-1,90582,39,-1,-1,0,0), -- Greater Dye of CON
(4520,-1,90582,40,-1,-1,0,0), -- Greater Dye of CON
(4521,-1,90582,41,-1,-1,0,0), -- Greater Dye of DEX
(4522,-1,90582,42,-1,-1,0,0), -- Greater Dye of DEX
(4523,-1,90582,43,-1,-1,0,0), -- Greater Dye of INT
(4524,-1,90582,44,-1,-1,0,0), -- Greater Dye of INT
(4525,-1,90582,45,-1,-1,0,0), -- Greater Dye of MEN
(4526,-1,90582,46,-1,-1,0,0), -- Greater Dye of MEN
(4527,-1,90582,47,-1,-1,0,0), -- Greater Dye of WIT
(4528,-1,90582,48,-1,-1,0,0), -- Greater Dye of WIT
(4529,-1,90582,49,-1,-1,0,0), -- Greater Dye of STR
(4530,-1,90582,50,-1,-1,0,0), -- Greater Dye of STR
(4531,-1,90582,51,-1,-1,0,0), -- Greater Dye of CON
(4532,-1,90582,52,-1,-1,0,0), -- Greater Dye of CON
(4533,-1,90582,53,-1,-1,0,0), -- Greater Dye of DEX
(4534,-1,90582,54,-1,-1,0,0), -- Greater Dye of DEX
(4535,-1,90582,55,-1,-1,0,0), -- Greater Dye of INT
(4536,-1,90582,56,-1,-1,0,0), -- Greater Dye of INT
(4537,-1,90582,57,-1,-1,0,0), -- Greater Dye of MEN
(4538,-1,90582,58,-1,-1,0,0), -- Greater Dye of MEN
(4539,-1,90582,59,-1,-1,0,0), -- Greater Dye of WIT
(4540,-1,90582,60,-1,-1,0,0), -- Greater Dye of WIT
(4541,-1,90582,61,-1,-1,0,0), -- Greater Dye of STR
(4542,-1,90582,62,-1,-1,0,0), -- Greater Dye of STR
(4543,-1,90582,63,-1,-1,0,0), -- Greater Dye of CON
(4544,-1,90582,64,-1,-1,0,0), -- Greater Dye of CON
(4545,-1,90582,65,-1,-1,0,0), -- Greater Dye of DEX
(4546,-1,90582,66,-1,-1,0,0), -- Greater Dye of DEX
(4547,-1,90582,67,-1,-1,0,0), -- Greater Dye of INT
(4548,-1,90582,68,-1,-1,0,0), -- Greater Dye of INT
(4549,-1,90582,69,-1,-1,0,0), -- Greater Dye of MEN
(4550,-1,90582,70,-1,-1,0,0), -- Greater Dye of MEN
(4551,-1,90582,71,-1,-1,0,0), -- Greater Dye of WIT
(4552,-1,90582,72,-1,-1,0,0), -- Greater Dye of WIT
(4553,-1,90582,73,-1,-1,0,0), -- Greater Dye of STR
(4554,-1,90582,74,-1,-1,0,0), -- Greater Dye of STR
(4555,-1,90582,75,-1,-1,0,0), -- Greater Dye of CON
(4556,-1,90582,76,-1,-1,0,0), -- Greater Dye of CON
(4557,-1,90582,77,-1,-1,0,0), -- Greater Dye of DEX
(4558,-1,90582,78,-1,-1,0,0), -- Greater Dye of DEX
(4559,-1,90582,79,-1,-1,0,0), -- Greater Dye of INT
(4560,-1,90582,80,-1,-1,0,0), -- Greater Dye of INT
(4561,-1,90582,81,-1,-1,0,0), -- Greater Dye of MEN
(4562,-1,90582,82,-1,-1,0,0), -- Greater Dye of MEN
(4563,-1,90582,83,-1,-1,0,0), -- Greater Dye of WIT
(4564,-1,90582,84,-1,-1,0,0), -- Greater Dye of WIT
(4565,-1,90582,85,-1,-1,0,0), -- Greater Dye of STR
(4566,-1,90582,86,-1,-1,0,0), -- Greater Dye of STR
(4567,-1,90582,87,-1,-1,0,0), -- Greater Dye of CON
(4568,-1,90582,88,-1,-1,0,0), -- Greater Dye of CON
(4569,-1,90582,89,-1,-1,0,0), -- Greater Dye of DEX
(4570,-1,90582,90,-1,-1,0,0), -- Greater Dye of DEX
(4571,-1,90582,91,-1,-1,0,0), -- Greater Dye of INT
(4572,-1,90582,92,-1,-1,0,0), -- Greater Dye of INT
(4573,-1,90582,93,-1,-1,0,0), -- Greater Dye of MEN
(4574,-1,90582,94,-1,-1,0,0), -- Greater Dye of MEN
(4575,-1,90582,95,-1,-1,0,0), -- Greater Dye of WIT
(4576,-1,90582,96,-1,-1,0,0), -- Greater Dye of WIT
(4577,-1,90582,97,-1,-1,0,0), -- Greater Dye of STR
(4578,-1,90582,98,-1,-1,0,0), -- Greater Dye of STR
(4579,-1,90582,99,-1,-1,0,0), -- Greater Dye of CON
(4580,-1,90582,100,-1,-1,0,0), -- Greater Dye of CON
(4581,-1,90582,101,-1,-1,0,0), -- Greater Dye of DEX
(4582,-1,90582,102,-1,-1,0,0), -- Greater Dye of DEX
(4583,-1,90582,103,-1,-1,0,0), -- Greater Dye of INT
(4584,-1,90582,104,-1,-1,0,0), -- Greater Dye of INT
(4585,-1,90582,105,-1,-1,0,0), -- Greater Dye of MEN
(4586,-1,90582,106,-1,-1,0,0), -- Greater Dye of MEN
(4587,-1,90582,107,-1,-1,0,0), -- Greater Dye of WIT
(4588,-1,90582,108,-1,-1,0,0), -- Greater Dye of WIT
(4589,-1,90582,109,-1,-1,0,0), -- Greater Dye of STR
(4590,-1,90582,110,-1,-1,0,0), -- Greater Dye of STR
(4591,-1,90582,111,-1,-1,0,0), -- Greater Dye of CON
(4592,-1,90582,112,-1,-1,0,0), -- Greater Dye of CON
(4593,-1,90582,113,-1,-1,0,0), -- Greater Dye of DEX
(4594,-1,90582,114,-1,-1,0,0), -- Greater Dye of DEX
(4595,-1,90582,115,-1,-1,0,0), -- Greater Dye of INT
(4596,-1,90582,116,-1,-1,0,0), -- Greater Dye of INT
(4597,-1,90582,117,-1,-1,0,0), -- Greater Dye of MEN
(4598,-1,90582,118,-1,-1,0,0), -- Greater Dye of MEN
(4599,-1,90582,119,-1,-1,0,0), -- Greater Dye of WIT
(4600,-1,90582,120,-1,-1,0,0), -- Greater Dye of WIT

-- 
(4601,-1,90583,1,-1,-1,0,0), -- Greater Dye of STR
(4602,-1,90583,2,-1,-1,0,0), -- Greater Dye of STR
(4603,-1,90583,3,-1,-1,0,0), -- Greater Dye of CON
(4604,-1,90583,4,-1,-1,0,0), -- Greater Dye of CON
(4605,-1,90583,5,-1,-1,0,0), -- Greater Dye of DEX
(4606,-1,90583,6,-1,-1,0,0), -- Greater Dye of DEX
(4607,-1,90583,7,-1,-1,0,0), -- Greater Dye of INT
(4608,-1,90583,8,-1,-1,0,0), -- Greater Dye of INT
(4609,-1,90583,9,-1,-1,0,0), -- Greater Dye of MEN
(4610,-1,90583,10,-1,-1,0,0), -- Greater Dye of MEN
(4611,-1,90583,11,-1,-1,0,0), -- Greater Dye of WIT
(4612,-1,90583,12,-1,-1,0,0), -- Greater Dye of WIT
(4613,-1,90583,13,-1,-1,0,0), -- Greater Dye of STR
(4614,-1,90583,14,-1,-1,0,0), -- Greater Dye of STR
(4615,-1,90583,15,-1,-1,0,0), -- Greater Dye of CON
(4616,-1,90583,16,-1,-1,0,0), -- Greater Dye of CON
(4617,-1,90583,17,-1,-1,0,0), -- Greater Dye of DEX
(4618,-1,90583,18,-1,-1,0,0), -- Greater Dye of DEX
(4619,-1,90583,19,-1,-1,0,0), -- Greater Dye of INT
(4620,-1,90583,20,-1,-1,0,0), -- Greater Dye of INT
(4621,-1,90583,21,-1,-1,0,0), -- Greater Dye of MEN
(4622,-1,90583,22,-1,-1,0,0), -- Greater Dye of MEN
(4623,-1,90583,23,-1,-1,0,0), -- Greater Dye of WIT
(4624,-1,90583,24,-1,-1,0,0); -- Greater Dye of WIT