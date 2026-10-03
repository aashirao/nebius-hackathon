import json, os, unittest
from unittest.mock import patch
import server
class Response:
    def __init__(self,content):self.content=content
    def __enter__(self):return self
    def __exit__(self,*args):pass
    def read(self):return json.dumps({'choices':[{'message':{'content':json.dumps(self.content)}}]}).encode()
class ClassificationTests(unittest.TestCase):
    def run_result(self,value):
        with patch.dict(os.environ,{'NEBIUS_API_KEY':'test'}),patch('urllib.request.urlopen',return_value=Response(value)):
            return server.classify({'merchant':'A Person'})
    def test_low_confidence_review(self):
        self.assertEqual(self.run_result({'category':'Travel','confidence':.5})['category'],'Needs review')
    def test_invalid_category(self):
        with self.assertRaises(ValueError):self.run_result({'category':'salary','confidence':1})
    def test_nan_confidence(self):
        with self.assertRaises(ValueError):self.run_result({'category':'Travel','confidence':float('nan')})
    def test_valid(self):self.assertEqual(self.run_result({'category':'Travel','confidence':.95})['provider'],'Nebius Token Factory')
    def test_no_key_is_not_fake_ai(self):
        with patch.dict(os.environ,{},clear=True),self.assertRaises(RuntimeError):server.classify({'merchant':'Uber'})
if __name__=='__main__':unittest.main()
