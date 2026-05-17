INSERT INTO game_systems (id, code, name, version, description)
VALUES
    (
        '7d4298e7-f415-49f8-a9dd-c5677d00ce90',
        'DND5E',
        'Dungeons & Dragons 5e',
        '2014',
        'Planned support for D&D 5e character management.'
    ),
    (
        '690b1746-fca0-4288-a21f-80855b0d46c2',
        'FATE_CORE',
        'Fate Core',
        '4th Edition',
        'Official Fate Core character creation and validation rules.'
    );

INSERT INTO character_templates (id, game_system_id, name, schema, version, is_official, visibility)
VALUES
    (
        '09eacc57-2ec8-45ba-89f3-2edbf989856a',
        '7d4298e7-f415-49f8-a9dd-c5677d00ce90',
        'Official D&D 5e Character Sheet',
        $$
        {
          "type": "dnd5e.character-sheet",
          "schemaVersion": 1,
          "gameSystemCode": "DND5E",
          "title": "Official D&D 5e Character Sheet",
          "status": "planned",
          "fields": [
            {
              "name": "name",
              "type": "string",
              "required": true
            },
            {
              "name": "class",
              "type": "string",
              "required": true
            },
            {
              "name": "level",
              "type": "int",
              "required": true
            },
            {
              "name": "attributes",
              "type": "object",
              "required": true
            }
          ]
        }
        $$::jsonb,
        1,
        TRUE,
        'visible'
    ),
    (
        'fdb6a7c1-aa64-48bf-8729-3873fb495516',
        '690b1746-fca0-4288-a21f-80855b0d46c2',
        'Official Fate Core Character Sheet',
        $$
        {
          "type": "fate-core.character-sheet",
          "schemaVersion": 2,
          "gameSystemCode": "FATE_CORE",
          "title": "Official Fate Core Character Sheet",
          "rules": {
            "engine": "FATE_CORE",
            "refresh": {
              "min": 0,
              "max": 10
            },
            "stress": {
              "min": 0,
              "max": 4
            },
            "consequences": {
              "mildSlots": 2
            }
          },
          "fields": [
            {
              "name": "name",
              "type": "string",
              "required": true
            },
            {
              "name": "description",
              "type": "string",
              "required": false
            },
            {
              "name": "aspects",
              "type": "object",
              "required": true,
              "properties": {
                "highConcept": "string",
                "trouble": "string",
                "additional": "string[]",
                "temporary": "string[]",
                "scene": "string[]"
              }
            },
            {
              "name": "skills",
              "type": "matrix<string,int>",
              "required": true
            },
            {
              "name": "stunts",
              "type": "string[]",
              "required": true
            },
            {
              "name": "refresh",
              "type": "int",
              "required": true,
              "constraints": {
                "min": 0,
                "max": 10
              }
            },
            {
              "name": "stress",
              "type": "object",
              "required": true,
              "properties": {
                "physical": {
                  "type": "int",
                  "min": 0,
                  "max": 4
                },
                "mental": {
                  "type": "int",
                  "min": 0,
                  "max": 4
                }
              }
            },
            {
              "name": "consequences",
              "type": "object",
              "required": true,
              "properties": {
                "mild": {
                  "type": "string-or-null[]",
                  "maxItems": 2
                },
                "moderate": "string|null",
                "severe": "string|null"
              }
            },
            {
              "name": "notes",
              "type": "string",
              "required": false
            }
          ]
        }
        $$::jsonb,
        2,
        TRUE,
        'visible'
    );
