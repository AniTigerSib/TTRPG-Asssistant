UPDATE character_templates
SET
    schema = $$
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
    version = 2,
    visibility = 'visible'
WHERE id = 'fdb6a7c1-aa64-48bf-8729-3873fb495516';
