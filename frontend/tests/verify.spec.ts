import { test, expect } from '@playwright/test';

test('homepage has expected content', async ({ page }) => {
  await page.goto('http://localhost:4173');
  await expect(page.locator('h1')).toHaveText('eSocial SST');
  await page.screenshot({ path: 'screenshot.png' });
});
