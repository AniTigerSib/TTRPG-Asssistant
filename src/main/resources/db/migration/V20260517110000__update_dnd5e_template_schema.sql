UPDATE game_systems
SET description = 'Official D&D 5e (2014) character sheets and dice expressions.'
WHERE id = '7d4298e7-f415-49f8-a9dd-c5677d00ce90';

UPDATE character_templates
SET
    schema = $$
    {
      "type": "dnd5e.character-sheet",
      "schemaVersion": 2,
      "gameSystemCode": "DND5E",
      "title": "Official D&D 5e Character Sheet (2014)",
      "status": "supported",
      "fields": [
        {
          "name": "name",
          "type": "string",
          "required": true
        },
        {
          "name": "playerName",
          "type": "string",
          "required": false
        },
        {
          "name": "className",
          "type": "string",
          "required": true
        },
        {
          "name": "subclass",
          "type": "string",
          "required": false
        },
        {
          "name": "level",
          "type": "int",
          "required": true
        },
        {
          "name": "background",
          "type": "string",
          "required": false
        },
        {
          "name": "race",
          "type": "string",
          "required": false
        },
        {
          "name": "alignment",
          "type": "string",
          "required": false
        },
        {
          "name": "experiencePoints",
          "type": "int",
          "required": false
        },
        {
          "name": "proficiencyBonus",
          "type": "int",
          "required": true
        },
        {
          "name": "armorClass",
          "type": "int",
          "required": false
        },
        {
          "name": "initiativeBonus",
          "type": "int",
          "required": false
        },
        {
          "name": "speedFeet",
          "type": "int",
          "required": false
        },
        {
          "name": "hitPoints",
          "type": "object",
          "required": false,
          "properties": {
            "maximum": "int|null",
            "current": "int|null",
            "temporary": "int|null"
          }
        },
        {
          "name": "hitDice",
          "type": "string",
          "required": false
        },
        {
          "name": "abilities",
          "type": "object",
          "required": true,
          "properties": {
            "strength": "int",
            "dexterity": "int",
            "constitution": "int",
            "intelligence": "int",
            "wisdom": "int",
            "charisma": "int"
          }
        },
        {
          "name": "savingThrows",
          "type": "matrix<string,int>",
          "required": false
        },
        {
          "name": "skills",
          "type": "matrix<string,int>",
          "required": false
        },
        {
          "name": "senses",
          "type": "string[]",
          "required": false
        },
        {
          "name": "languages",
          "type": "string[]",
          "required": false
        },
        {
          "name": "proficiencies",
          "type": "string[]",
          "required": false
        },
        {
          "name": "equipment",
          "type": "string[]",
          "required": false
        },
        {
          "name": "featuresAndTraits",
          "type": "string[]",
          "required": false
        },
        {
          "name": "spells",
          "type": "string[]",
          "required": false
        },
        {
          "name": "notes",
          "type": "string",
          "required": false
        }
      ]
    }
    $$::jsonb,
    version = 2
WHERE id = '09eacc57-2ec8-45ba-89f3-2edbf989856a';
