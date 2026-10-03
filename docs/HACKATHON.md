# Nebius x NVIDIA hackathon checklist

Checked against official pages on 3 October 2026:
- https://nebiusglobalaihackathon.devpost.com/
- https://nebiusglobalaihackathon.devpost.com/rules
- https://nebiusglobalaihackathon.devpost.com/resources

Track: Best Apps and Agents. Working title: Pocketwise. Positioning: student money management that starts from irregular real receipts, not an assumed salary.

Deadline: 30 October 2026, 10:00 am PDT = 10:30 pm India time. Verify the live rules before submission. Judging listed as 1–15 December 2026; keep judge access available until it ends.

## Must complete

- [ ] Register on Devpost; verify each entrant meets eligibility, age-of-majority, location and conflict-of-interest rules. If a team, authorize a representative.
- [ ] Build and test a functioning Android app on its intended platform, consistent with every demo claim.
- [ ] Make and verify a runtime inference call to a NVIDIA Nemotron model on Nebius Token Factory. A hardcoded or mocked classifier alone does not satisfy this requirement. Nebius hosting is encouraged but not mandatory when runtime inference meets the rule.
- [ ] Record actual model ID, endpoint, latency, failure behavior and examples. Do not fabricate feedback or accuracy.
- [x] Publish initial source, setup instructions and MIT license in https://github.com/aashirao/nebius-hackathon. Keep instructions aligned with the final tested app.
- [ ] Provide a working demo URL or Android test-build URL and clear testing instructions. Include necessary restricted judge credentials if private; do not expose unrestricted keys. Access must be free and available through judging.
- [ ] Upload a public YouTube demo under three minutes showing the Android app working and explaining actual Nebius/NVIDIA usage.
- [ ] Provide English project description, demo/testing instructions, chosen track, and honest feedback on tools used.
- [ ] Explain significant changes during the submission period if incorporating a pre-existing project. This source bundle was created for this request; retain commit history.
- [ ] Confirm rights to code, assets, model usage and any third-party integrations. Do not use unlicensed music or trademarks in the video.
- [ ] Complete required Devpost fields and submit before deadline. No submission has been made by this assistant.

## Judging alignment

Four equally weighted criteria: technical implementation, design, potential impact, quality of idea. Evidence to prepare:
- Correct transaction/goal accounting and error handling.
- Coherent onboarding and capture-consent flow tested on a phone.
- Student testing of irregular income flows; use actual anonymized feedback with consent, not invented quotes.
- Demonstrate unknown-payment review honestly, alongside fast automatic supported cases.

## Credits

The resources page lists $25 Token Factory credits via its linked form using activation code `NEBIUS-DEVPOST-GLOBAL26`, plus another $25 through the Builders Program. Availability and conditions must be checked on signup. Do not invent a plugin connection: Nebius is integrated through an API key configured as a server secret.

## Not yet compliant as a finished submission

Public source, a compiled Android debug APK, and passing backend/Java core tests are available. Device QA, live inference, backend deployment, durable judge test-build access, video, feedback and Devpost submission remain outstanding. This checklist is not a certification of compliance.
