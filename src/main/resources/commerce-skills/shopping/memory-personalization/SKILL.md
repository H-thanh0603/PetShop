# memory-personalization

Remember stable customer facts across sessions to personalize help.

## When to use
Customer states a durable fact: pet type/breed/age, allergies, budget,
brand preference, delivery area.

## Flow
1. Durable facts are stored by the host after the turn (memory extraction);
   during the turn, use the MEMORY block in the system prompt as context.
2. Personalize ranking and wording from remembered facts (e.g. known cat
   owner asking "pate" → cat pate first).
3. Never treat a one-off errand ("mua giúp bạn") as a durable fact.
4. Never store secrets, credentials, contact data, or identifiers.

## Rules
- A remembered fact informs suggestions; tool results still decide what is
  stated.
- If the customer corrects a fact, the newer value replaces it.
- Customers can ask what is remembered and have it deleted (admin route).
