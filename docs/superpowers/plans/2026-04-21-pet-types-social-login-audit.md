# Pet Types + Social Login + Ecommerce Audit

- Fixed admin pet-types JSP rendering bug caused by invalid JSTL `forEach` syntax.
- Hardened Google/Facebook login against missing OAuth config, missing email, and callback/token failures.
- Added `secrets.properties.example` and README instructions for OAuth setup.
- Replaced leftover admin `users-data.jsp` appointment/pet fields with ecommerce user stats.
